import africa.ejara.trustdart.Coin
import wallet.core.jni.CoinType

class TERRA : Coin("TERRA", CoinType.TERRA) {
    override fun signTransaction(
            path: String,
            txData: Map<String, Any>,
            mnemonic: String,
            passphrase: String
    ): String? {
        val wallet = HDWallet(mnemonic, passphrase)
        val privateKey = wallet.getKey(coinType, path)
        val key = PrivateKey(privateKey.toHexByteArray())

        val txAmount =
                Cosmos.Amount.newBuilder()
                        .apply {
                            amount = txData["amount"]
                            denom = "luna"
                        }
                        .build()

        val sendCoinsMsg =
                Cosmos.Message.Send.newBuilder()
                        .apply {
                            fromAddress = txData["ownerAddress"]
                            toAddress = txData["toAddress"]
                            addAllAmounts(listOf(txAmount))
                            typePrefix = "bank/MsgSend"
                        }
                        .build()

        val message = Cosmos.Message.newBuilder().apply { sendCoinsMessage = sendCoinsMsg }.build()

        val feeAmount =
                Cosmos.Amount.newBuilder()
                        .apply {
                            amount = txData["feeLimit"]
                            denom = "luna"
                        }
                        .build()

        val cosmosFee =
                Cosmos.Fee.newBuilder()
                        .apply {
                            gas = txData["cosmosFee"]
                            addAllAmounts(listOf(feeAmount))
                        }
                        .build()

        val signingInput =
                Cosmos.SigningInput.newBuilder()
                        .apply {
                            accountNumber = txData["accountNumber"]
                            chainId = txData["chainId"]
                            memo = txData["memo"]
                            sequence = txData["sequence"]
                            fee = cosmosFee
                            privateKey = ByteString.copyFrom(key.data())
                            addAllMessages(listOf(message))
                        }
                        .build()

        return AnySigner.sign(signingInput, TERRA, SigningOutput.parser())
        // val opJson = JSONObject(txData).toString()
        // return AnySigner.signJSON(opJson, privateKey.data(), coinType!!.value())
    }
}
