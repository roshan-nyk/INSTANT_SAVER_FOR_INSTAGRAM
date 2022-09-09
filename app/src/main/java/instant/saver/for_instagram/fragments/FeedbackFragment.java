package instant.saver.for_instagram.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import instant.saver.for_instagram.R;

import instant.saver.for_instagram.api.GetDataFromServer;
import instant.saver.for_instagram.util.Utils;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.Provider;
import java.security.Security;
import java.util.Arrays;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class FeedbackFragment extends Fragment {

    public FeedbackFragment() {
        // Required empty public constructor
    }

    public static FeedbackFragment newInstance() {
        FeedbackFragment fragment = new FeedbackFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_feedback, container, false);
    }

    private static boolean isSent = false;

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        EditText feedback = view.findViewById(R.id.feedback_fragment_feedback_edit_text);
        EditText email = view.findViewById(R.id.feedback_fragment_email_edit_text);
        Button submit = view.findViewById(R.id.feedback_fragment_Submit_Button);
        ImageView backButton = view.findViewById(R.id.feedback_fragment_Back_Button);
        Utils util = new Utils(requireActivity());
        backButton.setOnClickListener(v -> requireActivity().onBackPressed() );
        submit.setOnClickListener(v -> {
            util.hideSoftKeyboard(v);
            if (util.isNetworkAvailable()) {
                GetDataFromServer.getInstance().getDataBaseWriteExecutor().execute(() -> {
                        String suggestion = feedback.getText().toString();
                        if (suggestion.length() > 0) {
                            GMailSender sender = new GMailSender("nayak.803@yahoo.com", "cbfyeanuujovbseb");
                            try {
                                sender.sendMail("Feedback From An User Of Instant Saver",
                                        "Feedback:- " + suggestion + "\n\n" + "Email ID:- " + email.getText().toString(),
                                        "nayak.803@yahoo.com",
                                        "roshan.personified@protonmail.com");
                                if(isSent) {
                                    requireActivity().runOnUiThread(() -> Toast.makeText(requireActivity(), "Thank You For Your Valuable Feedback", Toast.LENGTH_SHORT).show());
                                    email.setText(null);
                                    feedback.setText(null);
                                }else
                                    requireActivity().runOnUiThread(() ->  Toast.makeText(requireActivity(), "Sorry, Couldn't Send Your Feedback Now.\nYou can still Email us @roshan.personified@protonmail.com", Toast.LENGTH_LONG).show());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else
                           requireActivity().runOnUiThread(() -> Toast.makeText(requireActivity(), "Kindly write your suggestion inside Suggestion Box", Toast.LENGTH_SHORT).show());
                      });
            } else
                Toast.makeText(requireActivity(), "No Internet Connection", Toast.LENGTH_SHORT).show();
        });
    }


    private static class GMailSender extends javax.mail.Authenticator {
        static {
            Security.addProvider(new JSSEProvider());
        }

        private final String mailhost = "smtp.mail.yahoo.com";
        private final String user;
        private final String password;
        private final javax.mail.Session session;

        GMailSender(String user, String password) {
            this.user = user;
            this.password = password;

            Properties props = new Properties();
            props.setProperty("mail.transport.protocol", "smtp");
            props.setProperty("mail.host", mailhost);
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.port", "465");
            props.put("mail.smtp.socketFactory.port", "465");
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.socketFactory.fallback", "false");
            props.setProperty("mail.smtp.quitwait", "false");

            session = Session.getDefaultInstance(props, this);
        }

        protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
            return new javax.mail.PasswordAuthentication(user, password);
        }

        public synchronized void sendMail(String subject, String body, String sender, String recipients) throws Exception {
            try {
                MimeMessage message = new MimeMessage(session);

                DataHandler handler = new DataHandler(new ByteArrayDataSource(body.getBytes(), "text/plain"));
                message.setSender(new InternetAddress(sender));
                message.setSubject(subject);
                message.setDataHandler(handler);
                if (recipients.indexOf(',') > 0)
                    message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipients));
                else
                    message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipients));

                Transport transport = session.getTransport("smtp");
                transport.connect(mailhost, 465, user, password);
                Log.d("TAG", "allrecipients: " + Arrays.toString(message.getAllRecipients()));
                transport.sendMessage(message, message.getAllRecipients());
                transport.close();
                isSent = true;
            } catch (Exception e) {
                Log.d("TAG", "sendMail: " + e.fillInStackTrace() + "\n" + e.getLocalizedMessage());
                isSent = false;
            }
        }

        private class ByteArrayDataSource implements DataSource {
            private byte[] data;
            private String type;

            public ByteArrayDataSource(byte[] data, String type) {
                super();
                this.data = data;
                this.type = type;
            }

            public ByteArrayDataSource(byte[] data) {
                super();
                this.data = data;
            }

            public void setType(String type) {
                this.type = type;
            }

            public String getContentType() {
                if (type == null)
                    return "application/octet-stream";
                else
                    return type;
            }

            public InputStream getInputStream() throws IOException {
                return new ByteArrayInputStream(data);
            }

            public String getName() {
                return "ByteArrayDataSource";
            }

            public OutputStream getOutputStream() throws IOException {
                throw new IOException("Not Supported");
            }
        }
    }

    private static final class JSSEProvider extends Provider {

        private JSSEProvider() {
            super("HarmonyJSSE", 1.0, "Harmony JSSE Provider");
            AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
                put("SSLContext.TLS",
                        "org.apache.harmony.xnet.provider.jsse.SSLContextImpl");
                put("Alg.Alias.SSLContext.TLSv1", "TLS");
                put("KeyManagerFactory.X509",
                        "org.apache.harmony.xnet.provider.jsse.KeyManagerFactoryImpl");
                put("TrustManagerFactory.X509",
                        "org.apache.harmony.xnet.provider.jsse.TrustManagerFactoryImpl");
                return null;
            });
        }
    }

}