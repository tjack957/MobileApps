package edu.uw.main.ui.auth.Login;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.auth0.android.jwt.JWT;

import org.json.JSONException;
import org.json.JSONObject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import edu.uw.main.PasswordValidator;
import edu.uw.main.R;
import edu.uw.main.databinding.FragmentLoginBinding;
import edu.uw.main.model.PushyTokenViewModel;
import edu.uw.main.model.UserInfoViewModel;
//import edu.uw.main.databinding.FragmentLoginBinding;

import static edu.uw.main.PasswordValidator.ValidationResult;
import static edu.uw.main.PasswordValidator.checkExcludeWhiteSpace;
import static edu.uw.main.PasswordValidator.checkPwdLength;
import static edu.uw.main.PasswordValidator.checkPwdSpecialChar;


/**
 * A fragment to handle the user login page.
 * @author Group 3
 * @version 5/5
 */
public class LoginFragment extends Fragment {
    /** Our login view model. */
    private LoginViewModel mLoginModel;

    private PushyTokenViewModel mPushyTokenViewModel;
    private UserInfoViewModel mUserViewModel;

    /** Our binding for this fragment. */
    private FragmentLoginBinding binding;

    /** Validates email field on the login fragment. Requirements: Special char (@), no white space, length > 0*/
    private PasswordValidator validate = checkPwdSpecialChar()
            .and(checkExcludeWhiteSpace())
            .and(checkPwdLength(0));

    /** Validates the password field on the login fragment. Requirements: NOt empty, length > 0 */
    private PasswordValidator pwdValidate = checkExcludeWhiteSpace()
            .and(checkPwdLength(0));

    /**
     * Default Constructor
     */
    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        mPushyTokenViewModel = new ViewModelProvider(getActivity())
                .get(PushyTokenViewModel.class);
        mLoginModel = new ViewModelProvider(getActivity())
                .get(LoginViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentLoginBinding.inflate(inflater, container, false);

        binding.layoutWait.setVisibility(View.INVISIBLE);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        binding.buttonRegister.setOnClickListener(button ->
                processRegister());
        binding.buttonSuccess.setOnClickListener(button ->
                processSuccess());
        binding.textRecover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigateToRecovery();
            }
        });

        binding.textResendVerification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigateToResendVerification();
            }
        });

        mLoginModel.addResponseObserver(
                getViewLifecycleOwner(),
                this::observeSignInResponse);

        mLoginModel.addResponseObserver(getViewLifecycleOwner(), response ->
                binding.layoutWait.setVisibility(View.GONE));


        mPushyTokenViewModel.addTokenObserver(getViewLifecycleOwner(), token ->
                binding.buttonSuccess.setEnabled(!token.isEmpty()));

        mPushyTokenViewModel.addResponseObserver(
                getViewLifecycleOwner(),
                this::observePushyPutResponse);
    }
    @Override
    public void onStart() {
        super.onStart();

        SharedPreferences prefs =
                getActivity().getSharedPreferences(
                        getString(R.string.keys_shared_prefs),
                        Context.MODE_PRIVATE);

        if (prefs.contains(getString(R.string.keys_prefs_jwt))) {
            String token = prefs.getString(getString(R.string.keys_prefs_jwt), "");
            JWT jwt = new JWT(token);
            // Check to see if the web token is still valid or not. To make a JWT expire after a
            // longer or shorter time period, change the expiration time when the JWT is
            // created on the web service.
            if(!jwt.isExpired(0)) {
                String email = jwt.getClaim("email").asString();
                success(email, token);
                return;
            }
        }
    }

    /**
     * Navigates to the register page when the register button is clicked.
     */
    public void processRegister(){

        Navigation.findNavController(getView()).navigate(
                LoginFragmentDirections.actionLoginFragmentToRegisterFragment2()
        );
    }

    private void navigateToRecovery() {
        Navigation.findNavController(getView()).navigate(LoginFragmentDirections.actionLoginFragmentToRecovery());
    }

    private void navigateToResendVerification() {
        Navigation.findNavController(getView()).navigate(LoginFragmentDirections.actionLoginFragmentToReVerification());
    }

    /**
     * Method to validate the email field. Calls the password validation method after email is validated.
     */
    public void processSuccess() {

        validate.processResult(validate
                        .apply(binding.textEmail.getText().toString())
                        .filter(result -> result != ValidationResult.SUCCESS),
                this::processPassword,
                this::handleEmailError);

    }

    /** Error thrown if email field is wrong. */
    private void handleEmailError(ValidationResult result) {
        String report = errorCode(result);
        binding.textEmail.setError(report);
    }

    /**
     * Validates password field and then calls a method to authenticate to the server.
     */
    private void processPassword(){
        pwdValidate.processResult(pwdValidate
                        .apply(binding.textPassword.getText().toString())
                        .filter(result -> result != ValidationResult.SUCCESS),
                this::verifyAuthWithServer,
                this::handleSuccessError);
    }

    /**
     * Error thrown if password field is wrong.
     * @param result - Error thrown.
     */
    private void handleSuccessError(ValidationResult result) {
        String report = errorCode(result);
        binding.textPassword.setError(report);
    }

    /**
     * Navigates to the success fragment with email and json web token params passed.
     */
    private void success(final String email, final String jwt) {
        binding.layoutWait.setVisibility(View.VISIBLE);
        String message = binding.textPassword.getText().toString();
        if (binding.switchSignin.isChecked()) {
            SharedPreferences prefs =
                    getActivity().getSharedPreferences(
                            getString(R.string.keys_shared_prefs),
                            Context.MODE_PRIVATE);
            //Store the credentials in SharedPrefs
            prefs.edit().putString(getString(R.string.keys_prefs_jwt), jwt).apply();
        }

        Navigation.findNavController(getView()).navigate(
                LoginFragmentDirections.actionLoginFragmentToSuccessFragment(
                        email,
                        jwt
                )
        );
        getActivity().finish();
    }

    /**
     * Returns error code based off of what fields failed the validation check.
     * @param result - result from our validation
     * @return - The error returned.
     */
    private String errorCode(ValidationResult result){
        switch(result){
            case PWD_MISSING_SPECIAL :
                return "Invalid: Email missing @ symbol.";
            case PWD_INCLUDES_WHITESPACE:
            case PWD_INVALID_LENGTH:
                return "Invalid: Field is empty or contains whitespace in text.";

        }
        return null;
    }

    /**
     * Method to connect to our webservice. Checks email and password in database.
     */
    private void verifyAuthWithServer() {
        mLoginModel.connect(
                binding.textEmail.getText().toString(),
                binding.textPassword.getText().toString());
    }

    /**
     * Method to inform user that they still need to validate their email account.
     */
    private void processToastValidate() {
        Toast toast = Toast.makeText(getActivity(), "Please Validate Your Email" , Toast.LENGTH_LONG);
        toast.setGravity(Gravity.BOTTOM, 0, 0);
        toast.show();
    }

    /**
     * Method to inform user that their password is incorrect.
     */
    private void processToastEmail() {
        Toast toast = Toast.makeText(getActivity(), "Incorrect password. Please try again" , Toast.LENGTH_LONG);
        toast.setGravity(Gravity.BOTTOM, 0, 0);
        toast.show();
    }

    /**
     * An observer on the HTTP Response from the web server. This observer should be
     * attached to SignInViewModel.
     *
     * @param response the Response from the server
     */
    private void observeSignInResponse(final JSONObject response) {
        if (response.length() > 0) {
            if (response.has("code")) {
                try {
                    binding.textEmail.setError(
                            "Error Authenticating: " +
                                    response.getJSONObject("data").getString("message"));

                } catch (JSONException e) {
                    Log.e("JSON1 Parse Error", e.getMessage());
                        if (e.getMessage().contains("Credentials")) {
                            processToastEmail();
                        } else if (e.getMessage().contains("Not")) {
                            processToastValidate();
                        }
                }
            } else {
                try {
                    mUserViewModel = new ViewModelProvider(getActivity(),
                            new UserInfoViewModel.UserInfoViewModelFactory(
                                    binding.textEmail.getText().toString(),
                                    response.getString("token")
                            )).get(UserInfoViewModel.class);
                    sendPushyToken();

                } catch (JSONException e) {
                    Log.e("JSON2 Parse Error", e.getMessage());

                }
            }
        } else {
            Log.d("JSON Response", "No Response");
        }
    }

    /**
     * Helper to abstract the request to send the pushy token to the web service
     */
    private void sendPushyToken() {
        mPushyTokenViewModel.sendTokenToWebservice(mUserViewModel.getmJwt());
    }

    /**
     * An observer on the HTTP Response from the web server. This observer should be
     * attached to PushyTokenViewModel.
     *
     * @param response the Response from the server
     */
    private void observePushyPutResponse(final JSONObject response) {
        if (response.length() > 0) {
            if (response.has("code")) {
                //this error cannot be fixed by the user changing credentials...
                binding.textEmail.setError(
                        "Error Authenticating on Push Token. Please contact support");
            } else {
                success(binding.textEmail.getText().toString(), mUserViewModel.getmJwt());
            }
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle("Login");
    }

}
