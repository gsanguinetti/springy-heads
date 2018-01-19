package com.flipkart.springyheads.demo;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.VisibleForTesting;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.flipkart.chatheads.ui.ChatHead;
import com.flipkart.chatheads.ui.ChatHeadViewAdapter;
import com.flipkart.chatheads.ui.MaximizedArrangement;
import com.flipkart.chatheads.ui.MinimizedArrangement;
import com.flipkart.chatheads.ui.container.DefaultChatHeadManager;
import com.flipkart.chatheads.ui.container.WindowManagerContainer;
import com.github.bassaer.chatmessageview.model.IChatUser;
import com.github.bassaer.chatmessageview.model.Message;
import com.github.bassaer.chatmessageview.view.ChatView;
import com.github.bassaer.chatmessageview.view.MessageView;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ChatHeadService extends Service {

    @VisibleForTesting
    protected static final int RIGHT_BUBBLE_COLOR = R.color.colorPrimaryDark;
    @VisibleForTesting
    protected static final int LEFT_BUBBLE_COLOR = R.color.gray300;
    @VisibleForTesting
    protected static final int BACKGROUND_COLOR = R.color.blueGray400;
    @VisibleForTesting
    protected static final int SEND_BUTTON_COLOR = R.color.blueGray500;
    @VisibleForTesting
    protected static final int SEND_ICON = R.drawable.ic_action_send;
    @VisibleForTesting
    protected static final int OPTION_BUTTON_COLOR = R.color.teal500;
    @VisibleForTesting
    protected static final int RIGHT_MESSAGE_TEXT_COLOR = Color.WHITE;
    @VisibleForTesting
    protected static final int LEFT_MESSAGE_TEXT_COLOR = Color.BLACK;
    @VisibleForTesting
    protected static final int USERNAME_TEXT_COLOR = Color.WHITE;
    @VisibleForTesting
    protected static final int SEND_TIME_TEXT_COLOR = Color.WHITE;
    @VisibleForTesting
    protected static final int DATA_SEPARATOR_COLOR = Color.WHITE;
    @VisibleForTesting
    protected static final int MESSAGE_STATUS_TEXT_COLOR = Color.WHITE;
    @VisibleForTesting
    protected static final String INPUT_TEXT_HINT = "New message..";
    @VisibleForTesting
    protected static final int MESSAGE_MARGIN = 5;

    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();
    private DefaultChatHeadManager<String> chatHeadManager;
    private int chatHeadIdentifier = 0;
    private WindowManagerContainer windowManagerContainer;
    private Map<String, View> viewCache = new HashMap<>();

    private ChatView mChatView;
    private MessageList mMessageList;
    private ArrayList<User> mUsers;

    private int mReplyDelay = -1;

    private static final int READ_REQUEST_CODE = 100;

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        initUsers();
        windowManagerContainer = new WindowManagerContainer(this);
        chatHeadManager = new DefaultChatHeadManager<String>(this, windowManagerContainer);
        chatHeadManager.setViewAdapter(new ChatHeadViewAdapter<String>() {

            @Override
            public View attachView(String key, ChatHead chatHead, ViewGroup parent) {
                View cachedView = viewCache.get(key);
                if (cachedView == null) {
                    LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                    View view = inflater.inflate(R.layout.fragment_chat, parent, false);

                    mChatView = (ChatView) view.findViewById(R.id.chat_view);

                    setMessageboxColors(mChatView);

                    //Load saved messages
                    loadMessages();

                    //Set UI parameters if you need
                    mChatView.setRightBubbleColor(ContextCompat.getColor(getApplicationContext(), R.color.blueGray50));
                    mChatView.setLeftBubbleColor(ContextCompat.getColor(getApplicationContext(), R.color.premiumChatDesignColor1));
                    mChatView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.softBackground));
                    mChatView.setSendButtonColor(Color.WHITE);
                    mChatView.setSendIcon(SEND_ICON);
                    mChatView.setOptionIcon(R.drawable.ic_microphone_off);
                    mChatView.setOptionButtonColor(Color.WHITE);
                    mChatView.setRightMessageTextColor(Color.WHITE);
                    mChatView.setLeftMessageTextColor(Color.WHITE);
                    mChatView.setUsernameTextColor(Color.BLACK);
                    mChatView.setSendTimeTextColor(Color.BLACK);
                    mChatView.setDateSeparatorColor(Color.BLACK);
                    mChatView.setMessageStatusTextColor(Color.BLACK);
                    mChatView.setInputTextHint(INPUT_TEXT_HINT);
                    mChatView.setMessageMarginTop(10);
                    mChatView.setMessageMarginBottom(10);
                    mChatView.setMaxInputLine(5);
                    mChatView.setUsernameFontSize(getResources().getDimension(R.dimen.font_small));

                    mChatView.setOnBubbleClickListener(new Message.OnBubbleClickListener() {
                        @Override
                        public void onClick(@NotNull Message message) {

                        }
                    });

                    mChatView.setOnBubbleLongClickListener(new Message.OnBubbleLongClickListener() {
                        @Override
                        public void onLongClick(@NotNull Message message) {

                        }
                    });

                    mChatView.setOnClickOptionButtonListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //new message
                            final Message message = new Message.Builder()
                                    .setUser(mUsers.get(0))
                                    .setRightMessage(true)
                                    .setMessageText(mChatView.getInputText())
                                    .hideIcon(false)
                                    .setStatusIconFormatter(new MyMessageStatusFormatter(getApplicationContext()))
                                    .setStatusTextFormatter(new MyMessageStatusFormatter(getApplicationContext()))
                                    .setMessageStatusType(Message.Companion.getMESSAGE_STATUS_ICON())
                                    .setStatus(MyMessageStatusFormatter.STATUS_DELIVERED)
                                    .build();

                            PremiumAssistant.INSTANCE.speech(getApplicationContext(), "aaaa", new PremiumAssistant.ReceiveMessageCallback() {
                                @Override
                                public void receive(String request, String message1) {
                                    //Set to chat view
                                    message.setMessageText(request);
                                    mChatView.send(message);
                                    //Add message list
                                    mMessageList.add(message);
                                    receiveMessage(message1);
                                }

                                @Override
                                public void onSpeechRecognitionStarted() {
                                    mChatView.setOptionIcon(R.drawable.ic_microphone_on);
                                }

                                @Override
                                public void onSpeechRecognitionFinished() {
                                    mChatView.setOptionIcon(R.drawable.ic_microphone_off);
                                }
                            });
                        }
                    });

                    //Click Send Button
                    mChatView.setOnClickSendButtonListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //new message
                            Message message = new Message.Builder()
                                    .setUser(mUsers.get(0))
                                    .setRightMessage(true)
                                    .setMessageText(mChatView.getInputText())
                                    .hideIcon(false)
                                    .setStatusIconFormatter(new MyMessageStatusFormatter(getApplicationContext()))
                                    .setStatusTextFormatter(new MyMessageStatusFormatter(getApplicationContext()))
                                    .setMessageStatusType(Message.Companion.getMESSAGE_STATUS_ICON())
                                    .setStatus(MyMessageStatusFormatter.STATUS_DELIVERED)
                                    .build();

                            //Set to chat view
                            mChatView.send(message);
                            //Add message list
                            mMessageList.add(message);
                            //Reset edit text
                            mChatView.setInputText("");

                            PremiumAssistant.INSTANCE.talk(getApplicationContext(), "aaaa", message.getMessageText(), new PremiumAssistant.ReceiveMessageCallback() {
                                @Override
                                public void receive(String request, String message) {
                                    receiveMessage(message);
                                }

                                @Override
                                public void onSpeechRecognitionStarted() {
                                    mChatView.setOptionIcon(R.drawable.ic_microphone_on);
                                }

                                @Override
                                public void onSpeechRecognitionFinished() {
                                    mChatView.setOptionIcon(R.drawable.ic_microphone_off);
                                }
                            });
                        }

                    });

                    cachedView = view;
                    viewCache.put(key, view);
                }
                parent.addView(cachedView);
                return cachedView;
            }

            @Override
            public void detachView(String key, ChatHead<? extends Serializable> chatHead, ViewGroup parent) {
                View cachedView = viewCache.get(key);
                if (cachedView != null) {
                    parent.removeView(cachedView);
                }
            }

            @Override
            public void removeView(String key, ChatHead<? extends Serializable> chatHead, ViewGroup parent) {
                View cachedView = viewCache.get(key);
                if (cachedView != null) {
                    viewCache.remove(key);
                    parent.removeView(cachedView);
                }
            }

            @Override
            public Drawable getChatHeadDrawable(String key) {
                return ChatHeadService.this.getChatHeadDrawable(key);
            }
        });

        addChatHead();
        chatHeadManager.setArrangement(MinimizedArrangement.class, null);
    }

    private Drawable getChatHeadDrawable(String key) {
        return ContextCompat.getDrawable(getApplicationContext(), R.drawable.chathead);

    }

    public void addChatHead() {
        addChatHead(false);
    }

    public void addChatHead(boolean sticky) {
        chatHeadIdentifier++;
        chatHeadManager.addChatHead(String.valueOf(chatHeadIdentifier), sticky, false);
        chatHeadManager.bringToFront(chatHeadManager.findChatHeadByKey(String.valueOf(chatHeadIdentifier)));
    }

    private void openGallery() {

    }

    private void receiveMessage(String sendText) {
        //Ignore hey
        if (!sendText.contains("hey")) {

            //Receive message
            final Message receivedMessage = new Message.Builder()
                    .setUser(mUsers.get(1))
                    .setRightMessage(false)
                    .setMessageText(sendText)
                    .setStatusIconFormatter(new MyMessageStatusFormatter(getApplicationContext()))
                    .setStatusTextFormatter(new MyMessageStatusFormatter(getApplicationContext()))
                    .setMessageStatusType(Message.Companion.getMESSAGE_STATUS_ICON())
                    .setStatus(MyMessageStatusFormatter.STATUS_DELIVERED)
                    .build();

            if (sendText.equals(Message.Type.PICTURE.name())) {
                receivedMessage.setMessageText("Nice!");
            }

            // This is a demo bot
            // Return within 3 seconds
            if (mReplyDelay < 0) {
                mReplyDelay = (new Random().nextInt(4) + 1) * 1000;
            }
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mChatView.receive(receivedMessage);
                    //Add message list
                    mMessageList.add(receivedMessage);
                }
            }, mReplyDelay);
        }
    }

    private void initUsers() {
        mUsers = new ArrayList<>();
        //User id
        int myId = 0;
        //User icon
        Bitmap myIcon = BitmapFactory.decodeResource(getResources(), R.drawable.csajszi);
        //User name
        String myName = "Adrienn Cseh";

        int yourId = 1;
        Bitmap yourIcon = BitmapFactory.decodeResource(getResources(), R.drawable.chathead);
        String yourName = "Jockey";

        final User me = new User(myId, myName, myIcon);
        final User you = new User(yourId, yourName, yourIcon);

        mUsers.add(me);
        mUsers.add(you);
    }

    /**
     * Load saved messages
     */
    private void loadMessages() {
        List<Message> messages = new ArrayList<>();
        mMessageList = AppData.getMessageList(this);
        if (mMessageList == null) {
            mMessageList = new MessageList();
        } else {
            for (int i = 0; i < mMessageList.size(); i++) {
                Message message = mMessageList.get(i);
                //Set extra info because they were removed before save messages.
                for (IChatUser user : mUsers) {
                    if (message.getUser().getId().equals(user.getId())) {
                        message.getUser().setIcon(user.getIcon());
                    }
                }
                if (!message.isDateCell() && message.isRightMessage()) {
                    message.hideIcon(true);

                }
                message.setMessageStatusType(Message.Companion.getMESSAGE_STATUS_ICON_RIGHT_ONLY());
                message.setStatusIconFormatter(new MyMessageStatusFormatter(this));
                message.setStatus(MyMessageStatusFormatter.STATUS_DELIVERED);
                messages.add(message);
            }
        }
        MessageView messageView = mChatView.getMessageView();
        messageView.init(messages);
        messageView.setSelection(messageView.getCount() - 1);
    }

    public void removeChatHead() {
        chatHeadManager.removeChatHead(String.valueOf(chatHeadIdentifier), true);
        chatHeadIdentifier--;
    }

    public void removeAllChatHeads() {
        chatHeadIdentifier = 0;
        chatHeadManager.removeAllChatHeads(true);
    }

    public void toggleArrangement() {
        if (chatHeadManager.getActiveArrangement() instanceof MinimizedArrangement) {
            chatHeadManager.setArrangement(MaximizedArrangement.class, null);
        } else {
            chatHeadManager.setArrangement(MinimizedArrangement.class, null);
        }
    }

    public void updateBadgeCount() {
        chatHeadManager.reloadDrawable(String.valueOf(chatHeadIdentifier));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        windowManagerContainer.destroy();
    }

    public void minimize() {
        chatHeadManager.setArrangement(MinimizedArrangement.class, null);
    }

    public void setMessageboxColors(ChatView chatView) {
        EditText editText = (EditText) chatView.findViewById(R.id.inputBox);
        editText.setHintTextColor(Color.WHITE);
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        ChatHeadService getService() {
            // Return this instance of LocalService so clients can call public methods
            return ChatHeadService.this;
        }
    }
}