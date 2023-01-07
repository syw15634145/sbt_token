package com.example.demo;

import com.algorand.algosdk.account.Account;
import com.algorand.algosdk.algod.client.ApiException;
import com.algorand.algosdk.crypto.Address;
import com.algorand.algosdk.transaction.SignedTransaction;
import com.algorand.algosdk.transaction.Transaction;
import com.algorand.algosdk.util.Encoder;
import com.algorand.algosdk.v2.client.Utils;
import com.algorand.algosdk.v2.client.common.AlgodClient;
import com.algorand.algosdk.v2.client.common.Response;
import com.algorand.algosdk.v2.client.model.PendingTransactionResponse;
import com.algorand.algosdk.v2.client.model.PostTransactionsResponse;
import com.algorand.algosdk.v2.client.model.TransactionParametersResponse;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import static com.algorand.algosdk.util.Digester.digest;

public class SBTManager {
    private Account account;
    private AlgodClient client;


    public SBTManager() throws GeneralSecurityException {
        account = new Account("blanket erupt math cargo stay trophy give shell mix nominee margin gain zoo trumpet arrow always secret between sound visual gentle amount million able olympic");
        client = connectToNetwork();
    }
    public String getAddress() {
        return account.getAddress().toString();
    }
    public String mintSBT() throws Exception {

        AlgodClient client = connectToNetwork();

        // get changing network parameters for each transaction
        Response<TransactionParametersResponse> resp = client.TransactionParams().execute();
        if (!resp.isSuccessful()) {
            throw new Exception(resp.message());
        }
        TransactionParametersResponse params = resp.body();
        if (params == null) {
            throw new Exception("Params retrieval error");
        }

        // Create the Asset:
        boolean defaultFrozen = true;
        String unitName = "SBT";
        String assetName = "CS294-SBT";
        String url = "https://s3.amazonaws.com/your-bucket/metadata.json";
        byte[] imageFile = Files.readAllBytes(Paths.get("src/main/resources/static/sbt.png"));
        byte[] imgHash = digest(imageFile);
        String imgSRI = "sha256-" + Base64.getEncoder().encodeToString(imgHash);


        byte[] metadataFILE = Files.readAllBytes(Paths.get("src/main/resources/static/NFTmetadata.json"));
        // use this to verify that the metadatahash displayed in the asset creation response is correct
        // cat metadata.json | openssl dgst -sha256 -binary | openssl base64 -A
        byte[] assetMetadataHash = digest(metadataFILE);
        String assetMetadataHashString = assetMetadataHash.toString();
        Address manager = account.getAddress();  // OPTIONAL: FOR DEMO ONLY, USED TO DESTROY ASSET WITHIN THIS SCRIPT
        Address reserve = account.getAddress();;
        Address freeze = account.getAddress();;
        Address clawback = account.getAddress();;

        BigInteger assetTotal = BigInteger.valueOf(1);
        // decimals and assetTotal
        Integer decimals = 0;

        Transaction tx = Transaction.AssetCreateTransactionBuilder()
                .sender(account.getAddress().toString())
                .assetTotal(assetTotal)
                .assetDecimals(decimals)
                .assetUnitName(unitName)
                .assetName(assetName)
                .url(url)
                .metadataHash(assetMetadataHash)
                .manager(manager)
                .reserve(reserve)
                .freeze(freeze)
                .defaultFrozen(defaultFrozen)
                .clawback(clawback)
                .suggestedParams(params)
                .build();

        // Sign the Transaction with creator account
        SignedTransaction signedTxn = account.signTransaction(tx);
        Long assetID = null;

        try {
            // Submit the transaction to the network
            String[] headers = { "Content-Type" };
            String[] values = { "application/x-binary" };
            // Submit the transaction to the network
            byte[] encodedTxBytes = Encoder.encodeToMsgPack(signedTxn);
            Response<PostTransactionsResponse> rawtxresponse = client.RawTransaction().rawtxn(encodedTxBytes)
                    .execute(headers, values);
            if (!rawtxresponse.isSuccessful()) {
                throw new Exception(rawtxresponse.message());
            }
            String id = rawtxresponse.body().txId;

            // Wait for transaction confirmation
            PendingTransactionResponse pTrx = Utils.waitForConfirmation(client,id,4);
            System.out.println("Transaction " + id + " confirmed in round " + pTrx.confirmedRound);

            assetID = pTrx.assetIndex;
            System.out.println("AssetID = " + assetID);
            return assetID.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return e.toString();
        }
    }
    // utility function to connect to a node
    private AlgodClient connectToNetwork() {
        final String ALGOD_API_TOKEN = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
        final String ALGOD_API_ADDR = "http://localhost";
        final Integer ALGOD_PORT = 4001;
        AlgodClient client = new AlgodClient(ALGOD_API_ADDR, ALGOD_PORT, ALGOD_API_TOKEN);
        return client;
    }
    public String getNote() throws IOException {
        byte[] metadataFILE = Files.readAllBytes(Paths.get("src/main/resources/static/NFTmetadata.json"));
        String metadataStr = new String(metadataFILE, StandardCharsets.UTF_8);
        return metadataStr;
    }
    public void transferSBT(String address, String assetId) throws Exception {
        Response<TransactionParametersResponse> resp = client.TransactionParams().execute();
        if (!resp.isSuccessful()) {
            throw new Exception(resp.message());
        }
        TransactionParametersResponse params = resp.body();
        if (params == null) {
            throw new Exception("Params retrieval error");
        }
        // params.fee = (long) 1000;
        // set asset specific parameters
        BigInteger assetAmount = BigInteger.valueOf(1);
        byte[] metadataFILE = Files.readAllBytes(Paths.get("src/main/resources/static/NFTmetadata.json"));
        String metadataStr = new String(metadataFILE, StandardCharsets.UTF_8);
        Transaction tx = Transaction.AssetClawbackTransactionBuilder()
                .sender(account.getAddress())
                .assetClawbackFrom(account.getAddress())
                .assetReceiver(address)
                .note(metadataFILE)
                .assetAmount(assetAmount)
                .assetIndex(Integer.valueOf(assetId))
                .suggestedParams(params)
                .build();
        // The transaction must be signed by the clawback account
        SignedTransaction signedTx = account.signTransaction(tx);
        // send the transaction to the network and
        // wait for the transaction to be confirmed
        try {
            String id = submitTransaction(signedTx);
            System.out.println("Transaction ID: " + id);
            PendingTransactionResponse pTrx = Utils.waitForConfirmation(client,id,4);
            System.out.println("Transaction " + id + " confirmed in round " + pTrx.confirmedRound);
            // list the account information for acct1 and acct3
            System.out.println("Account 3  = " + address);
            System.out.println("Account 1  = " + account.getAddress().toString());
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e.toString());
        }

    }

    public void revokeSBT(String address, String assetId) throws Exception {
        Response<TransactionParametersResponse> resp = client.TransactionParams().execute();
        if (!resp.isSuccessful()) {
            throw new Exception(resp.message());
        }
        TransactionParametersResponse params = resp.body();
        if (params == null) {
            throw new Exception("Params retrieval error");
        }
        // params.fee = (long) 1000;
        // set asset specific parameters
        BigInteger assetAmount = BigInteger.valueOf(1);

        Transaction tx = Transaction.AssetClawbackTransactionBuilder()
                .sender(account.getAddress().toString())
                .assetClawbackFrom(address)
                .assetReceiver(account.getAddress().toString())
                .assetAmount(assetAmount)
                .assetIndex(Integer.valueOf(assetId))
                .suggestedParams(params)
                .build();
        // The transaction must be signed by the clawback account
        SignedTransaction signedTx = account.signTransaction(tx);
        // send the transaction to the network and
        // wait for the transaction to be confirmed
        try {
            String id = submitTransaction(signedTx);
            System.out.println("Transaction ID: " + id);
            PendingTransactionResponse pTrx = Utils.waitForConfirmation(client,id,4);
            System.out.println("Transaction " + id + " confirmed in round " + pTrx.confirmedRound);
            // list the account information for acct1 and acct3
            System.out.println("Account 3  = " + address);
            System.out.println("Account 1  = " + account.getAddress().toString());
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    // Utility function for sending a raw signed transaction to the network
    private String submitTransaction(SignedTransaction signedTx) throws Exception {
        try {
            // Msgpack encode the signed transaction
            byte[] encodedTxBytes = Encoder.encodeToMsgPack(signedTx);
            String[] headers = {"Content-Type"};
            String[] values = {"application/x-binary"};
            Response < PostTransactionsResponse > rawtxresponse =
                    client.RawTransaction().rawtxn(encodedTxBytes).execute(headers, values);
            if (!rawtxresponse.isSuccessful()) {
                throw new Exception(rawtxresponse.message());
            }
            String id = rawtxresponse.body().txId;

            // String id = client.RawTransaction().rawtxn(encodedTxBytes).execute().body().txId;
            // ;
            return (id);
        } catch (ApiException e) {
            throw (e);
        }
    }

//    public List<SBTitem> searchSBT(String address, String assetId) {
//        List<SBTitem> list;
//
//    }


}
