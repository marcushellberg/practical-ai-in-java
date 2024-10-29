package org.vaadin.marcus.enterpriseai.view;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.Route;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.core.io.InputStreamResource;
import org.springframework.util.MimeTypeUtils;

import java.math.BigDecimal;
import java.util.List;

@Route
@Menu(title = "Receipt Scan", order = 4)
public class ReceiptScan extends VerticalLayout {
    public record LineItem(String name, int quantity, BigDecimal price) {}
    public record Receipt(String merchant, BigDecimal total, List<LineItem> lineItems) {}


    public ReceiptScan(ChatModel chatModel) {
        var client = ChatClient.builder(chatModel).build();

        var buffer = new MemoryBuffer();
        var upload = new Upload(buffer);

        upload.addSucceededListener(e -> {
            var receipt = client.prompt()
                .user(userMessage -> userMessage
                    .text("Please read the attached receipt and return the value in provided format")
                    .media(MimeTypeUtils.IMAGE_JPEG, new InputStreamResource(buffer.getInputStream())))
                .call()
                .entity(Receipt.class);

            showReceipt(receipt);
            upload.clearFileList();
        });

        add(upload);
    }

    private void showReceipt(Receipt receipt) {
        var items = new Grid<>(LineItem.class);
        items.setItems(receipt.lineItems());

        add(
            new H3("Receipt details"),
            new Paragraph("Merchant: " + receipt.merchant()),
            new Paragraph("Total: " + receipt.total()),
            items
        );
    }
}
