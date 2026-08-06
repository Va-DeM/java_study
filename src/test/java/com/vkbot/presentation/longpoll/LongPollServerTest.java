package com.vkbot.presentation.longpoll;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vkbot.presentation.dto.MessageDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MockitoExtension.class)
class LongPollServerTest {

    private static final long GROUP_ID = 100L;
    private final ObjectMapper mapper = new ObjectMapper();

    @Mock
    private VkApiClient vkApiClient;
    @Mock
    private GroupActor groupActor;

    private LongPollServer server;

    @BeforeEach
    void setUp() {
        server = new LongPollServer(vkApiClient, groupActor, GROUP_ID);
    }

    private JsonNode json(String content) throws Exception {
        return mapper.readTree(content);
    }

    @Test
    void parseUpdate_returnsMessageDtoForRegularMessage() throws Exception {
        JsonNode update = json("""
                {"type": "message_new", "object": {"message": {"id": 5, "from_id": 42, "text": "/start"}}}
                """);

        MessageDTO message = server.parseUpdate(update);

        assertEquals(42L, message.getUserId());
        assertEquals(5L, message.getMessageId());
        assertEquals("/start", message.getText());
    }

    @Test
    void parseUpdate_supportsLegacyFlatFormat() throws Exception {
        JsonNode update = json("""
                {"type": "message_new", "object": {"id": 7, "from_id": 42, "text": "hi"}}
                """);

        MessageDTO message = server.parseUpdate(update);

        assertEquals(42L, message.getUserId());
        assertEquals(7L, message.getMessageId());
        assertEquals("hi", message.getText());
    }

    @Test
    void parseUpdate_ignoresTypingStateEvents() throws Exception {
        JsonNode update = json("""
                {"type": "message_typing_state", "object": {"from_id": 42}}
                """);

        assertNull(server.parseUpdate(update));
    }

    @Test
    void parseUpdate_ignoresReadReceipts() throws Exception {
        JsonNode update = json("""
                {"type": "message_read", "object": {"from_id": 42}}
                """);

        assertNull(server.parseUpdate(update));
    }

    @Test
    void parseUpdate_ignoresMessageReplyEcho() throws Exception {
        JsonNode update = json("""
                {"type": "message_reply", "object": {"from_id": 42}}
                """);

        assertNull(server.parseUpdate(update));
    }

    @Test
    void parseUpdate_returnsNullWhenObjectMissing() throws Exception {
        JsonNode update = json("{\"type\": \"message_new\"}");

        assertNull(server.parseUpdate(update));
    }

    @Test
    void parseUpdate_returnsNullWhenFromIdCannotBeDetermined() throws Exception {
        JsonNode update = json("""
                {"type": "message_new", "object": {"message": {"id": 5, "text": "hi"}}}
                """);

        assertNull(server.parseUpdate(update));
    }

    @Test
    void parseUpdate_ignoresMessagesFromTheCommunityItself() throws Exception {
        JsonNode update = json("""
                {"type": "message_new", "object": {"message": {"id": 5, "from_id": -100, "text": "auto-reply"}}}
                """);

        assertNull(server.parseUpdate(update));
    }

    @Test
    void parseUpdate_returnsNullForTextlessMessages() throws Exception {
        JsonNode update = json("""
                {"type": "message_new", "object": {"message": {"id": 5, "from_id": 42}}}
                """);

        assertNull(server.parseUpdate(update));
    }
}
