package com.bot;

import com.codepoetics.protonpack.collectors.CompletableFutures;
import com.microsoft.bot.builder.*;
import com.microsoft.bot.schema.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class RequestLunch extends ActivityHandler {
    private static final String WELCOMEMESSAGE = "Welcome to Cafe Cammi.";
    private ConversationState conversationState;
    private UserState userState;

    @Autowired
    public RequestLunch(ConversationState withConversationState, UserState withUserState) {
        conversationState = withConversationState;
        userState = withUserState;
    }

    @Override
    public CompletableFuture<Void> onTurn(TurnContext turnContext) {
        return super.onTurn(turnContext)
                .thenCompose(turnResult -> conversationState.saveChanges(turnContext))
                .thenCompose(saveResult -> userState.saveChanges(turnContext));
    }

    @Override
    protected CompletableFuture<Void> onMembersAdded(List<ChannelAccount> membersAdded, TurnContext turnContext) {
        return sendWelcomeMessage(turnContext);
    }

    @Override
    protected CompletableFuture<Void> onMessageActivity(TurnContext turnContext) {
        String text = turnContext.getActivity().getText();
        // Take the input from the user and create the appropriate response.
        StatePropertyAccessor<CafeOrder> stateAccessor = conversationState.createProperty("cafeOrderState");
        CompletableFuture<CafeOrder> stateFuture = stateAccessor.get(turnContext, CafeOrder::new);

        return stateFuture.thenApply(cafeOrder -> {
            if (cafeOrder.getMenuItem() == null) {
                String responseText = processMenuItem(text, cafeOrder);

                if (!"Please select meal from the above choices".equalsIgnoreCase(responseText)) {
                    cafeOrder.setMenuItem(responseText);
                }
                return turnContext.sendActivities(
                        MessageFactory.text(responseText)
                );
            } else {
                Pair<Boolean, String> responseText = processQuantity(text);

                if (responseText.getLeft()) {
                    cafeOrder.setQuantity(responseText.getRight());

                    String response = "Your total amount is: Ksh " + cafeOrder.getTotal();


                    return turnContext.sendActivities(
                            MessageFactory.text(response), createSuggestedActions()
                    );

                } else {

                    return turnContext.sendActivities(
                            MessageFactory.text("Invalid quantity, please retry")
                    );
                }
            }
        })
                // make the return value happy.
                .thenApply(resourceResponse -> null);
    }

    private CompletableFuture<Void> sendWelcomeMessage(TurnContext turnContext) {
        return turnContext.getActivity().getMembersAdded().stream()
                .filter(member -> !StringUtils.equals(member.getId(), turnContext.getActivity().getRecipient().getId()))
                .map(channel -> turnContext.sendActivities(
                        MessageFactory.text(WELCOMEMESSAGE),
                        createSuggestedActions()
                ))
                .collect(CompletableFutures.toFutureList())
                .thenApply(resourceResponses -> null);
    }

    private Activity createSuggestedActions() {
        Activity reply = MessageFactory.text("What Would you Like to Order for today?");
        reply.setSuggestedActions(new SuggestedActions() {{
            setActions(Arrays.asList(
                    new CardAction() {{
                        setTitle("Ugali + Beef @ 300ksh");
                        setType(ActionTypes.IM_BACK);
                        setValue("Ugali + Beef");
                    }},
                    new CardAction() {{
                        setTitle("Rice + Beef @ 250ksh");
                        setType(ActionTypes.IM_BACK);
                        setValue("Rice + Beef");
                    }},
                    new CardAction() {{
                        setTitle("Rice + Beans @ 200ksh");
                        setType(ActionTypes.IM_BACK);
                        setValue("Rice + Beans");
                    }}
            ));
        }});
        return reply;
    }

    private String processMenuItem(String text, CafeOrder cafeOrder) {
        String quantity = "How many plates of ";
        switch (text) {
            case "Ugali + Beef":
                cafeOrder.setPrice(300d);
                return quantity + "Ugali Beef";

            case "Rice + Beef":
                cafeOrder.setPrice(250d);
                return quantity + "Rice Beef";

            case "Rice + Beans":
                cafeOrder.setPrice(200d);
                return quantity + "Rice Beans";

            default:
                return "Please select meal from the above choices";
        }
    }

    private Pair<Boolean, String> processQuantity(String response) {
        try {
            int quantity = Integer.parseInt(response);
            if (quantity > 3)
                return Pair.of(false, "We only serve a maximum of 3 plates");

            return Pair.of(true, Integer.toString(quantity));
        } catch (Exception e) {
            return Pair.of(false, "Enter a valid quantity");
        }
    }

}
