package org.erachain.webserver;

import org.erachain.api.ApiErrorFactory;
import org.erachain.api.CoreResource;
import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.block.Block;
import org.erachain.core.crypto.Base32;
import org.erachain.core.crypto.Base58;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.Order;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.core.transaction.Transaction;
import org.erachain.core.transaction.TransactionFactory;
import org.erachain.datachain.*;
import org.erachain.dbs.IteratorCloseable;
import org.erachain.lang.Lang;
import org.erachain.network.Peer;
import org.erachain.utils.StrJSonFine;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;
import org.mapdb.Fun.Tuple4;
import org.mapdb.Fun.Tuple5;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;




@SuppressWarnings({"unchecked", "rawtypes"})

@Path("api")
public class API {

    private static final Logger LOGGER = LoggerFactory.getLogger(API.class);
    @Context
    private UriInfo uriInfo;
    private HttpServletRequest request;
    private DCSet dcSet = DCSet.getInstance();
    private Controller cntrl = Controller.getInstance();

    @GET
    public Response Default() {

        Map help = new LinkedHashMap();

        help.put("see /apiasset", "Help for assets API");
        help.put("see /apipoll", "Help for polls API");
        help.put("see /apiperson", "Help for persons API");
        help.put("see /apitemplate", "Help for templates API");
        help.put("see /apistatus", "Help for statuses API");

        help.put("see /apitelegrams", "Help for telegrams API");
        help.put("see /apiexchange", "Help for exchange API");
        help.put("see /api/tx", "Help for transactions API");
        help.put("see /apidocuments", "Help for documents API");
        help.put("see /apifpool", "Help for forging pool API");

        help.put("*** BALANCE STRUCTURE ***", "");
        help.put("balance Side", "Balance has Sides: - BigDecimal array: [Total_Income (side 1), Remaining_Balance (side 2)]. Side 3 - Total Outcome as difference: Side1 - Side2");
        help.put("balance Positions", "Balances Array - BigDecimal array: [Balance in Own (pos 1), Balance in Debt (pos 2), Balance on Hold (pos 2), Balance of Spend/Consumer 9pos4), Balance of Pledge (pos 5)]. Pledge balance used for DEX orders too");

        help.put("*** CHAIN ***", "");
        help.put("GET Height", "height");
        help.put("GET Node Peers", "peers");
        help.put("GET First Block or Block.Head", "firstblock[?onlyhead]");
        help.put("GET Last Block", "lastblock[?onlyhead]");
        help.put("GET Last Block Head", "lastblockhead");

        help.put("*** BLOCK ***", "");
        help.put("GET Block", "block/{signature}[?onlyhead]");
        help.put("GET Block by Height", "blockbyheight/{height}[?onlyhead]");
        help.put("GET Child Block Signature", "childblocksignature/{signature}");
        help.put("GET Child Block", "childblock/{signature}[?onlyhead]");

        help.put("*** BLOCKS ***", "");
        help.put("GET Blocks from Height by Limit", "blocks?from={height}&offset={0}&limit=50&onlyhead&desc");
        help.put("GET Blocks from Height by Limit (end:1 if END is reached)", "blocksfromheight/{height}/{limit}?onlyhead&desc={false}");
        help.put("GET Blocks Signatures from Height by Limit (end:1 if END id reached)", "/blockssignaturesfromheight/{height}/{limit}");

        help.put("*** ADDRESS ***", "");
        help.put("GET Address Validate", "addressvalidate/{address}");
        help.put("GET Address Last Reference", "addresslastreference/{address}");
        help.put("GET Address Unconfirmed Last Reference", "addressunconfirmedlastreference/{address}/{from}/{count}");
        help.put("GET Address Generating Balance", "addressgeneratingbalance/{address}");
        help.put("GET Address Asset Balance", "addressassetbalance/{address}/{assetid}");
        help.put("GET Address FEE Statistics", "addressfeestats/{address}");

        help.put("GET Address Assets", "addressassets/{address}");
        help.put("GET Public Key by Address", "addresspublickey/{address}");
        help.put("GET Address Forging Info", "addressforge/{address}");
        help.put("GET Address Person Info", "addressasperson/{address}");
        help.put("GET Address Person Name", "addressaspersonlite/{address}");

        help.put("*** EXCHANGE ***", "");
        help.put("GET Exchange Orders", "exchangeorders/{have}/{want}");

        help.put("*** PERSON ***", "");
        help.put("GET Person Key by PubKey of Owner", "personkeybyownerpublickey/{publickey}");
        help.put("GET Person Key by Address", "personkeybyaddress/{address}");
        help.put("GET Person by Address", "personbyaddress/{address}");
        help.put("GET Person Key by Public Key", "personkeybypublickey/{publickey}");
        help.put("GET Person by Public Key", "personbypublickey/{publickey}");
        help.put("GET Person by Public Key Base32", "personbypublickeybase32/{publickeybase32}");
        help.put("GET Accounts From Person", "getaccountsfromperson/{key}");

        help.put("*** TOOLS ***", "");
        help.put("POST Verify Signature for JSON {'message': ..., 'signature': Base58, 'publickey': Base58)", "verifysignature");
        help.put("GET info by node", " GET api/info");
        help.put("GET benchmark info by node", " GET api/bench");

        help.put("GET Broadcasttelegram", "/broadcasttelegram/{raw(Base58)}?lang=en|ru");
        help.put("GET Broadcasttelegram64", "/broadcasttelegram64/{raw(Base64)}?lang=en|ru");
        help.put("POST Broadcasttelegram", "/broadcasttelegram?lang=en|ru raw(Base58)");
        help.put("POST Broadcasttelegram64", "/broadcasttelegram64?lang=en|ru raw(Base64)");
        help.put("POST broadcasttelegramjson", "See 'POST Broadcasttelegram'. Body is JSON: {\"raw\":\"Base58\"}");
        help.put("POST broadcasttelegram64json", "See 'POST Broadcasttelegram64'. Body is JSON: {\"raw\":\"Base64\"}");

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(StrJSonFine.convert(help))
                .build();
    }


    @GET
    @Path("height")
    public static Response getHeight() {
        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(String.valueOf(Controller.getInstance().getMyHeight()))
                .build();
    }

    @GET
    @Path("peers")
    public static Response getPeers() {

        JSONArray array = new JSONArray();
        for (Peer peer : Controller.getInstance().network.getKnownPeers()) {
            array.add(peer.toJson());
        }

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(array.toJSONString())
                .build();
    }

    @GET
    @Path("firstblock")
    public Response getFirstBlock(@Context UriInfo info) {

        boolean onlyhead = checkBoolean(info, "onlyhead");

        JSONObject out;
        if (onlyhead) {
            out = dcSet.getBlocksHeadsMap().get(1).toJson();
        } else {
            out = dcSet.getBlockMap().getAndProcess(1).toJson();
        }

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(out.toJSONString())
                .build();
    }

    @GET
    @Path("lastblock")
    public Response lastBlock(@Context UriInfo info) {

        boolean onlyhead = checkBoolean(info, "onlyhead");

        JSONObject out;
        if (onlyhead) {
            out = dcSet.getBlocksHeadsMap().last().toJson();
        } else {
            out = dcSet.getBlockMap().last().toJson();
        }

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(out.toJSONString())
                .build();
    }

    @GET
    @Path("lastblockhead")
    public Response lastBlockHead() {

        Block.BlockHead lastBlock = dcSet.getBlocksHeadsMap().last();
        JSONObject out = lastBlock.toJson();

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(out.toJSONString())
                .build();
    }

    @GET
    @Path("/childblocksignature/{signature}")
    public Response getChildBlockSignature(@PathParam("signature") String signature) {
        //DECODE SIGNATURE
        byte[] signatureBytes;
        JSONObject out = new JSONObject();

        int step = 1;
        try {
            signatureBytes = Base58.decode(signature);

            ++step;
            Integer heightWT = dcSet.getBlockSignsMap().get(signatureBytes);
            if (heightWT != null && heightWT > 0) {
                byte[] childSign = dcSet.getBlocksHeadsMap().get(heightWT + 1).signature;
                out.put("child", Base58.encode(childSign));
            } else {
                out.put("message", "signature not found");
            }
        } catch (Exception e) {
            out.put("error", step);
            if (step == 1)
                out.put("message", "signature error, use Base58 value");
            else if (step == 2)
                out.put("message", "child not found");
            else
                out.put("message", e.getMessage());
        }

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(out.toJSONString())
                .build();
    }

    @GET
    @Path("/childblock/{signature}")
    public Response getChildBlock(@Context UriInfo info,
                                  @PathParam("signature") String signature) {
        //DECODE SIGNATURE
        byte[] signatureBytes;
        JSONObject out = new JSONObject();

        boolean onlyhead = checkBoolean(info, "onlyhead");

        int step = 1;
        try {
            signatureBytes = Base58.decode(signature);

            ++step;
            Integer heightWT = dcSet.getBlockSignsMap().get(signatureBytes);
            if (heightWT != null && heightWT > 0) {
                if (onlyhead)
                    out = dcSet.getBlocksHeadsMap().get(heightWT + 1).toJson();
                else
                    out = dcSet.getBlockMap().get(heightWT + 1).toJson();
            } else {
                out.put("message", "signature not found");
            }
        } catch (Exception e) {
            out.put("error", step);
            if (step == 1)
                out.put("message", "signature error, use Base58 value");
            else if (step == 2)
                out.put("message", "child not found");
            else
                out.put("message", e.getMessage());
        }

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(out.toJSONString())
                .build();
    }

    @GET
    @Path("block/{signature}")
    public Response block(@Context UriInfo info,
                          @PathParam("signature") String signature) {

        boolean onlyhead = checkBoolean(info, "onlyhead");

        JSONObject out = new JSONObject();

        int step = 1;

        try {
            byte[] key = Base58.decode(signature);

            ++step;
            if (onlyhead) {
                Integer height = dcSet.getBlockSignsMap().get(key);
                Block.BlockHead blockHead = dcSet.getBlocksHeadsMap().get(height);
                out.put("blockHead", blockHead.toJson());

                ++step;
                byte[] childSign = dcSet.getBlocksHeadsMap().get(blockHead.heightBlock + 1).signature;
                if (childSign != null)
                    out.put("next", Base58.encode(childSign));
            } else {
                Block block = dcSet.getBlockSignsMap().getBlock(key);
                out.put("block", block.toJson());

                ++step;
                byte[] childSign = dcSet.getBlocksHeadsMap().get(block.getHeight() + 1).signature;
                if (childSign != null)
                    out.put("next", Base58.encode(childSign));
            }

        } catch (Exception e) {

            out.put("error", step);
            if (step == 1)
                out.put("message", "signature error, use Base58 value");
            else if (step == 2)
                out.put("message", "block not found");
            else
                out.put("message", e.getMessage());
        }

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(out.toJSONString())
                .build();
    }

    @GET
    @Path("blockbyheight/{height}")
    public Response blockByHeight(@Context UriInfo info,
                                  @PathParam("height") String heightStr) {

        boolean onlyhead = checkBoolean(info, "onlyhead");

        JSONObject out = new JSONObject();
        int step = 1;

        try {
            int height = Integer.parseInt(heightStr);

            ++step;
            if (onlyhead) {
                Block.BlockHead blockHead = dcSet.getBlocksHeadsMap().get(height);
                out.put("blockHead", blockHead.toJson());

                ++step;
                byte[] childSign = dcSet.getBlocksHeadsMap().get(blockHead.heightBlock + 1).signature;
                if (childSign != null)
                    out.put("next", Base58.encode(childSign));
            } else {

                Block block = cntrl.getBlockByHeight(dcSet, height);
                out.put("block", block.toJson());

                ++step;
                byte[] childSign = dcSet.getBlocksHeadsMap().get(block.getHeight() + 1).signature;
                if (childSign != null)
                    out.put("next", Base58.encode(childSign));
            }

        } catch (Exception e) {

            out.put("error", step);
            if (step == 1)
                out.put("message", "height error, use integer value");
            else if (step == 2)
                out.put("message", "block not found");
            else
                out.put("message", e.getMessage());
        }


        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(out.toJSONString())
                .build();
    }

    @GET
    @Path("blockbyheight2/{height}")
    public Response blockByHeight2(@PathParam("height") String heightStr) {

        JSONObject out = new JSONObject();
        int step = 1;

        try {
            int height = Integer.parseInt(heightStr);

            ++step;
            Block block;
            LinkedList eee = null;
            ListIterator listIterator = eee.listIterator(height);
            block = (Block) listIterator.next();

            //block = dcSet.getBlocksHeadMap().get(iterator.next());
            out.put("block", block.toJson());

            ++step;
            byte[] childSign = dcSet.getBlocksHeadsMap().get(block.getHeight() + 1).signature;
            if (childSign != null)
                out.put("next", Base58.encode(childSign));

        } catch (Exception e) {

            out.put("error", step);
            if (step == 1)
                out.put("message", "height error, use integer value " + e.getMessage());
            else if (step == 2)
                out.put("message", "block not found " + e.getMessage());
            else
                out.put("message", e.getMessage());
        }


        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(out.toJSONString())
                .build();
    }

    @GET
    @Path("/blocksfromheight/{height}/{limit}")
    public Response getBlocksFromHeightV2(@Context UriInfo info,
                                          @PathParam("height") Integer fromHeight,
                                          @DefaultValue("10") @PathParam("limit") int limit) {

        boolean onlyhead = checkBoolean(info, "onlyhead");
        boolean desc = checkBoolean(info, "desc");

        int limitTo = onlyhead ? 200 : 50;
        if (limit > limitTo)
            limit = limitTo;

        JSONObject out = new JSONObject();

        JSONArray array = new JSONArray();
        if (onlyhead) {
            BlocksHeadsMap blockHeadsMap = dcSet.getBlocksHeadsMap();
            if (true) {
                try {
                    try (IteratorCloseable<Integer> iterator = blockHeadsMap.getIterator(fromHeight, desc)) {
                        while (iterator.hasNext() && limit-- > 0) {
                            array.add(blockHeadsMap.get(iterator.next()).toJson());
                        }
                        if (limit > 0) {
                            out.put("end", 1);
                        }
                    }
                } catch (IOException e) {
                }
            } else {
                // OLD
                int max = blockHeadsMap.size();
                for (int i = fromHeight; i < fromHeight + limit; i++) {
                    if (i > max) {
                        out.put("end", 1);
                        break;
                    }
                    array.add(blockHeadsMap.get(i).toJson());
                }
            }
            out.put("blockHeads", array);

        } else {
            BlockMap blockMap = dcSet.getBlockMap();
            if (true) {
                try {
                    try (IteratorCloseable<Integer> iterator = blockMap.getIterator(fromHeight, desc)) {
                        int txCount = 0;
                        while (iterator.hasNext() && limit-- > 0) {
                            Block block = blockMap.get(iterator.next());
                            array.add(block.toJson());
                            txCount += block.getTransactionCount();
                            if (txCount > 10000) {
                                limit = 0;
                                out.put("broken", "by tx count: " + txCount);
                                break;
                            }
                        }
                        if (limit > 0) {
                            out.put("end", 1);
                        }
                    }
                } catch (IOException e) {
                }
            } else {
                // OLD
                int max = blockMap.size();
                for (int i = fromHeight; i < fromHeight + limit; i++) {
                    if (i > max) {
                        out.put("end", 1);
                        break;
                    }
                    array.add(blockMap.getAndProcess(i).toJson());
                }
            }
            out.put("blocks", array);
        }

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(out.toString())
                .build();
    }

    @GET
    @Path("blocks")
    public Response getBlocksFromHeightV1(@Context UriInfo info,
                                          @QueryParam("from") Integer fromHeight,
                                          @QueryParam("offset") int offset,
                                          @DefaultValue("10") @QueryParam("limit") int limit) {
        boolean onlyHead = checkBoolean(info, "onlyhead");
        boolean desc = checkBoolean(info, "desc");

        int limitMax = onlyHead ? 200 : 50;
        if (limit > limitMax)
            limit = limitMax;
        if (offset > limitMax)
            offset = limitMax;

        JSONArray array = new JSONArray();
        if (onlyHead) {
            BlocksHeadsMap blockHeadsMap = dcSet.getBlocksHeadsMap();
            try {
                try (IteratorCloseable<Integer> iterator = blockHeadsMap.getIterator(fromHeight, desc)) {
                    while (iterator.hasNext() && limit > 0) {
                        if (offset-- > 0) {
                            iterator.next();
                            continue;
                        }
                        limit--;
                        array.add(blockHeadsMap.get(iterator.next()).toJson());
                    }
                }
            } catch (IOException e) {
            }

        } else {
            BlockMap blockMap = dcSet.getBlockMap();
            try {
                try (IteratorCloseable<Integer> iterator = blockMap.getIterator(fromHeight, desc)) {
                    int txCount = 0;
                    while (iterator.hasNext() && limit > 0) {
                        if (offset-- > 0) {
                            iterator.next();
                            continue;
                        }
                        limit--;
                        Block block = blockMap.get(iterator.next());
                        array.add(block.toJson());
                        txCount += block.getTransactionCount();
                        if (txCount > 10000) {
                            //out.put("broken", "by tx count: " + txCount);
                            break;
                        }
                    }
                }
            } catch (IOException e) {
            }
        }
        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(array.toJSONString())
                .build();
    }

    @GET
    @Path("/blockssignaturesfromheight/{height}/{limit}")
    public Response getBlocksSignsFromHeight(@PathParam("height") int height,
                                             @PathParam("limit") int limit) {

        if (limit > 100)
            limit = 100;

        JSONObject out = new JSONObject();
        int step = 1;

        try {

            JSONArray array = new JSONArray();
            BlocksHeadsMap blocksHeadsMap = dcSet.getBlocksHeadsMap();
            int max = dcSet.getBlockMap().size();
            for (int i = height; i < height + limit; i++) {
                if (i > max) {
                    out.put("end", 1);
                    break;
                }
                array.add(Base58.encode(blocksHeadsMap.get(i).signature));
            }
            out.put("signatures", array);

        } catch (Exception e) {

            out.put("error", step);
            if (step == 1)
                out.put("message", "height error, use integer value");
            else if (step == 2)
                out.put("message", "block not found");
            else
                out.put("message", e.getMessage());
        }

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(out.toJSONString())
                .build();
    }


    /*
     * ************** RECORDS **********
     */

    @Deprecated
    @POST
    @Path("recordparse")
    public Response recordParse(String raw) // throws JSONException
    {

        JSONObject out = new JSONObject();

        //CREATE TRANSACTION FROM RAW
        Transaction transaction = null;
        try {
            transaction = TransactionFactory.getInstance().parse(Base58.decode(raw), Transaction.FOR_NETWORK);
            try {
                out = transaction.toJson();
            } catch (Exception e) {
                transaction.updateMapByError(-1, e.toString(), out);
            }
        } catch (Exception e) {
            out.put("error", -1);
            out.put("message", e.toString());
        }


        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(out.toJSONString())
                .build();
    }


    @Deprecated
    @GET
    @Path("recordparse/{raw}")
    public Response recordParseGET(@PathParam("raw") String raw) // throws JSONException
    {
        return recordParse(raw);
    }

    @GET
    @Path("record/{signature}")
    public Response record(@PathParam("signature") String signature) {

        JSONObject out = new JSONObject();

        int step = 1;

        try {
            byte[] key = Base58.decode(signature);

            ++step;
            Transaction record = cntrl.getTransaction(key, dcSet);
            out = record.toJson();

        } catch (Exception e) {

            out.put("error", step);
            if (step == 1)
                out.put("message", "signature error, use Base58 value");
            else if (step == 2)
                out.put("message", "record not found");
            else
                out.put("message", e.getMessage());
        }

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(out.toJSONString())
                .build();
    }

    @GET
    @Path("recordbynumber/{number}")
    public Response recodByNumber(@PathParam("number") String numberStr) {

        JSONObject out = new JSONObject();
        int step = 1;

        try {

            String[] strA = numberStr.split("\\-");
            int height = Integer.parseInt(strA[0]);
            int seq = Integer.parseInt(strA[1]);

            ++step;
            Transaction record = dcSet.getTransactionFinalMap().get(height, seq);
            out = record.toJson();

        } catch (Exception e) {

            out.put("error", step);
            if (step == 1)
                out.put("message", "height-sequence error, use integer-integer value");
            else if (step == 2)
                out.put("message", "record not found");
            else
                out.put("message", e.getMessage());
        }


        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(out.toJSONString())
                .build();
    }

    @Deprecated
    @GET
    @Path("recordraw/{signature}")
    public Response recordRaw(@PathParam("signature") String signature) {

        JSONObject out = new JSONObject();

        int step = 1;

        byte[] key;
        Transaction record = null;
        try {
            key = Base58.decode(signature);
            ++step;
            record = cntrl.getTransaction(key, dcSet);

            ++step;
            if (record == null) {
                out.put("error", step);
                out.put("message", "record not found");
            } else {
                ++step;
                try {
                    out = record.rawToJson();
                } catch (Exception e) {
                    out.put("error", step);
                    out.put("message", e.getMessage());
                }
            }

        } catch (Exception e) {
            out.put("error", step);
            if (step == 1)
                out.put("message", "signature error, use Base58 value");
            else
                out.put("message", e.getMessage());
        }

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(out.toJSONString())
                .build();
    }

    @Deprecated
    @GET
    @Path("recordrawbynumber/{number}")
    public Response recodRawByNumber(@PathParam("number") String numberStr) {

        JSONObject out = new JSONObject();
        int step = 1;

        try {

            String[] strA = numberStr.split("\\-");
            int height = Integer.parseInt(strA[0]);
            int seqNo = Integer.parseInt(strA[1]);

            ++step;
            Transaction record = dcSet.getTransactionFinalMap().get(height, seqNo);

            ++step;
            out = record.rawToJson();

        } catch (Exception e) {

            out.put("error", step);
            if (step == 1)
                out.put("message", "height-sequence error, use integer-integer value");
            else if (step == 2)
                out.put("message", "record not found");
            else
                out.put("message", e.getMessage());
        }

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(out.toJSONString())
                .build();
    }

    @Deprecated
    @GET
    @Path("recordrawbynumber/{block}/{seqNo}")
    public Response recodRawBySeqNo(@PathParam("block") int block, @PathParam("seqNo") int seqNo) {

        JSONObject out = new JSONObject();

        try {

            Transaction record = dcSet.getTransactionFinalMap().get(block, seqNo);
            out = record.rawToJson();

        } catch (Exception e) {
            out.put("message", e.getMessage());
        }


        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(out.toJSONString())
                .build();
    }

    @Deprecated
    @GET
    @Path("broadcast/{raw}")
    // http://127.0.0.1:9047/api/broadcast/DPDnFCNvPk4m8GMi2ZprirSgQDwxuQw4sWoJA3fmkKDrYwddTPtt1ucFV4i45BHhNEn1W1pxy3zhRfpxKy6fDb5vmvQwwJ3M3E12jyWLBJtHRYPLnRJnK7M2x5MnPbvnePGX1ahqt7PpFwwGiivP1t272YZ9VKWWNUB3Jg6zyt51fCuyDCinLx4awQPQJNHViux9xoGS2c3ph32oi56PKpiyM
    public Response broadcastRaw(@PathParam("raw") String raw, @QueryParam("lang") String lang) {


        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(broadcastFromRawString(raw, false, lang).toJSONString())
                .build();
    }

    @Deprecated
    @GET
    @Path("broadcast64/{raw}")
    public Response broadcastRaw64(@PathParam("raw") String raw, @QueryParam("lang") String lang) {


        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(broadcastFromRawString(raw, true, lang).toJSONString())
                .build();
    }

    @Deprecated
    @POST
    @Path("broadcastjson")
    public Response broadcastFromRawJsonPost(@Context HttpServletRequest request,
                                             MultivaluedMap<String, String> form) {

        String raw = form.getFirst("raw");
        String lang = form.getFirst("lang");

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(broadcastFromRawString(raw, false, lang).toJSONString())
                .build();

    }

    @Deprecated
    @POST
    @Path("broadcastjson64")
    public Response broadcastFromRaw64JsonPost(@Context HttpServletRequest request,
                                               MultivaluedMap<String, String> form) {

        String raw = form.getFirst("raw");
        String lang = form.getFirst("lang");

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(broadcastFromRawString(raw, true, lang).toJSONString())
                .build();

    }

    @Deprecated
    @POST
    @Path("broadcast")
    public Response broadcastFromRawPost(@Context HttpServletRequest request,
                                         @QueryParam("lang") String lang,
                                         String raw) {

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(broadcastFromRawString(raw, false, lang).toJSONString())
                .build();

    }

    @Deprecated
    @POST
    @Path("broadcast64")
    public Response broadcastFromRaw64Post(@Context HttpServletRequest request,
                                           @QueryParam("lang") String lang,
                                           String raw) {

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(broadcastFromRawString(raw, true, lang).toJSONString())
                .build();

    }

    // http://127.0.0.1:9047/api/broadcast?data=DPDnFCNvPk4m8GMi2ZprirSgQDwxuQw4sWoJA3fmkKDrYwddTPtt1ucFV4i45BHhNEn1W1pxy3zhRfpxKy6fDb5vmvQwwJ3M3E12jyWLBJtHRYPLnRJnK7M2x5MnPbvnePGX1ahqt7PpFwwGiivP1t272YZ9VKWWNUB3Jg6zyt51fCuyDCinLx4awQPQJNHViux9xoGS2c3ph32oi56PKpiyM
    @Deprecated
    public static JSONObject broadcastFromRawByte(byte[] transactionBytes, String lang) {
        Tuple3<Transaction, Integer, String> result = Controller.getInstance().lightCreateTransactionFromRaw(transactionBytes, false);
        if (result.a == null) {
            JSONObject langObj = Lang.getInstance().getLangJson(lang);
            JSONObject out = new JSONObject();
            Transaction.updateMapByErrorSimple(result.b, langObj == null || result.c == null ?
                    result.c : Lang.T(result.c, langObj), out);
            return out;

        }

        JSONObject out = result.a.toJson();
        if (result.b == Transaction.VALIDATE_OK) {
            out.put("status", "ok");
            return out;
        }

        result.a.updateMapByError(result.b, out, lang);

        return out;

    }

    @Deprecated
    public static JSONObject broadcastFromRawString(String rawDataStr, boolean base64, String lang) {
        JSONObject out = new JSONObject();
        byte[] transactionBytes;
        try {
            if (base64) {
                transactionBytes = Base64.getDecoder().decode(rawDataStr);
            } else {
                transactionBytes = Base58.decode(rawDataStr);
            }
        } catch (Exception e) {
            Transaction.updateMapByErrorSimple(-1, "JSON error", out);
            return out;
        }

        if (transactionBytes == null) {
            Transaction.updateMapByErrorSimple(-1, "JSON error", out);
            return out;
        }

        return broadcastFromRawByte(transactionBytes, lang);
    }

    @Deprecated
    public JSONObject broadcastTelegramBytes(byte[] transactionBytes, String lang) {
        JSONObject out = new JSONObject();
        Transaction transaction;

        try {
            transaction = TransactionFactory.getInstance().parse(transactionBytes, Transaction.FOR_NETWORK);
        } catch (Exception e) {
            Transaction.updateMapByErrorSimple(-1, e.toString() + " parse ERROR", out);
            return out;
        }

        // CHECK IF RECORD VALID
        if (!transaction.isSignatureValid(null, true)) {
            transaction.updateMapByError(-1, "INVALID_SIGNATURE", out);
            return out;
        }

        int result = Controller.getInstance().broadcastTelegram(transaction, true);
        if (result == 0) {
            out.put("status", "ok");
        } else {
            transaction.updateMapByError(result, out);
        }
        out.put("signature", Base58.encode(transaction.getSignature()));
        return out;
    }

    public JSONObject broadcastTelegramStr(String rawDataStr, boolean base64, String lang) {
        JSONObject out = new JSONObject();
        byte[] transactionBytes;
        try {
            if (base64) {
                transactionBytes = Base64.getDecoder().decode(rawDataStr);
            } else {
                transactionBytes = Base58.decode(rawDataStr);
            }
        } catch (Exception e) {
            Transaction.updateMapByErrorSimple(-1, "JSON error", out);
            return out;
        }

        if (transactionBytes == null) {
            Transaction.updateMapByErrorSimple(-1, "JSON error", out);
            return out;
        }

        return broadcastTelegramBytes(transactionBytes, lang);
    }

    @GET
    //@Path("broadcasttelegram/{raw}")
    @Path("broadcasttelegram/{raw}")
    // http://127.0.0.1:9047/broadcasttelegram/DPDnFCNvPk4m8GMi2ZprirSgQDwxuQw4sWoJA3fmkKDrYwddTPtt1ucFV4i45BHhNEn1W1pxy3zhRfpxKy6fDb5vmvQwwJ3M3E12jyWLBJtHRYPLnRJnK7M2x5MnPbvnePGX1ahqt7PpFwwGiivP1t272YZ9VKWWNUB3Jg6zyt51fCuyDCinLx4awQPQJNHViux9xoGS2c3ph32oi56PKpiyM
    public Response broadcastTelegram(@Context HttpServletRequest request,
                                      @QueryParam("lang") String lang,
                                      @PathParam("raw") String raw) {


        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(broadcastTelegramStr(raw, false, lang).toJSONString())
                .build();
    }

    @GET
    @Path("broadcasttelegram64/{raw}")
    public Response broadcastTelegram64(@Context HttpServletRequest request,
                                        @QueryParam("lang") String lang,
                                        @PathParam("raw") String raw) {


        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(broadcastTelegramStr(raw, true, lang).toJSONString())
                .build();
    }

    @POST
    @Path("broadcasttelegram")
    public Response broadcastTelegramPost(@Context HttpServletRequest request,
                                          @QueryParam("lang") String lang,
                                          String raw) {

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(broadcastTelegramStr(raw, false, lang).toJSONString())
                .build();

    }

    @POST

    @Path("broadcasttelegram64")
    public Response broadcastTelegram64Post(@Context HttpServletRequest request,
                                            @QueryParam("lang") String lang,
                                            String raw) {

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(broadcastTelegramStr(raw, true, lang).toJSONString())
                .build();

    }

    @POST
    @Path("broadcasttelegramjson")
    public Response broadcastTelegramPost(@Context HttpServletRequest request,
                                          @QueryParam("lang") String lang,
                                          MultivaluedMap<String, String> form) {

        String raw = form.getFirst("raw");

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(broadcastTelegramStr(raw, false, lang).toJSONString())
                .build();

    }

    @POST
    @Path("broadcasttelegramjson64")
    public Response broadcastTelegram64Post(@Context HttpServletRequest request,
                                            @QueryParam("lang") String lang,
                                            MultivaluedMap<String, String> form) {

        String raw = form.getFirst("raw");

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(broadcastTelegramStr(raw, true, lang).toJSONString())
                .build();

    }


    /*
     * ********** ADDRESS **********
     */
    // TODO переименовать бы LastTimestamp - так более понятно
    @GET
    @Path("addresslastreference/{address}")
    public Response getAddressLastReference(@PathParam("address") String address) {

        // CHECK IF VALID ADDRESS
        Tuple2<Account, String> result = Account.tryMakeAccount(address);
        if (result.a == null) {
            throw ApiErrorFactory.getInstance().createError(
                    Transaction.INVALID_ADDRESS);
        }

        // GET ACCOUNT
        Account account = result.a;

        long[] lastTimestamp = account.getLastTimestamp();

        String out;
        if (lastTimestamp == null) {
            out = "-";
        } else {
            out = "" + lastTimestamp[0];
        }

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(out)
                .build();

    }

    @Deprecated
    @GET
    @Path("addressunconfirmedlastreference/{address}/{from}/{count}")
    public Response getUnconfirmedLastReferenceUnconfirmed(@PathParam("address") String address,
                                                           @PathParam("from") int from, @PathParam("count") int count) {

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity("Deprecated")
                .build();

    }


    @GET
    @Path("addressvalidate/{address}")
    public Response validate(@PathParam("address") String address) {
        // CHECK IF VALID ADDRESS
        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(String.valueOf(Crypto.getInstance().isValidAddress(address)))
                .build();
    }

    @GET
    @Path("addressgeneratingbalance/{address}")
    public Response getAddressGeneratingBalanceOfAddress(
            @PathParam("address") String address) {
        // CHECK IF VALID ADDRESS
        Tuple2<Account, String> result = Account.tryMakeAccount(address);
        if (result.a == null) {
            throw ApiErrorFactory.getInstance().createError(
                    Transaction.INVALID_ADDRESS);

        }

        Account account = result.a;

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity("" + BlockChain.calcWinValue(DCSet.getInstance(),
                        account, Controller.getInstance().getBlockChain().getHeight(DCSet.getInstance()),
                        account.getBalanceUSE(Transaction.RIGHTS_KEY, dcSet).intValue(), null))
                .build();
    }

    @GET
    @Path("addressassetbalance/{address}/{assetid}")
    public Response getAddressAssetBalance(@PathParam("address") String address,
                                           @PathParam("assetid") String assetid) {
        // CHECK IF VALID ADDRESS
        Tuple2<Account, String> result = Account.tryMakeAccount(address);
        if (result.a == null) {
            throw ApiErrorFactory.getInstance().createError(
                    Transaction.INVALID_ADDRESS);

        }

        Long assetAsLong;

        // HAS ASSET NUMBERFORMAT
        try {
            assetAsLong = Long.valueOf(assetid);
        } catch (NumberFormatException e) {
            throw ApiErrorFactory.getInstance().createError(
                    Transaction.ITEM_ASSET_NOT_EXIST);
        }

        // DOES ASSETID EXIST
        if (!DCSet.getInstance().getItemAssetMap().contains(assetAsLong)) {
            throw ApiErrorFactory.getInstance().createError(
                    Transaction.ITEM_ASSET_NOT_EXIST);
        }


        Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> balance
                = result.a.getBalance(assetAsLong);
        JSONArray array = new JSONArray();

        array.add(setJSONArray(balance.a));
        array.add(setJSONArray(balance.b));
        array.add(setJSONArray(balance.c));
        array.add(setJSONArray(balance.d));
        array.add(setJSONArray(balance.e));

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(array.toJSONString())
                .build();
    }

    @GET
    @Path("addressfeestats/{address}")
    public Response getAddressFEEStats(@PathParam("address") String address) {
        // CHECK IF VALID ADDRESS
        Tuple2<Account, String> result = Account.tryMakeAccount(address);
        if (result.a == null) {
            throw ApiErrorFactory.getInstance().createError(
                    Transaction.INVALID_ADDRESS);

        }

        Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> balance
                = result.a.getBalance(AssetCls.FEE_KEY);

        JSONObject out = new JSONObject();

        out.put("referalAndGift", balance.c.a);
        out.put("totalEarn", balance.c.b);
        out.put("forged", balance.c.b.subtract(balance.c.a));
        out.put("totalSpend", balance.d.b);
        out.put("result", balance.c.b.subtract(balance.d.b));

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(out.toJSONString())
                .build();
    }

    @GET
    @Path("addressassets/{address}")
    public Response getAddressAssetBalance(@PathParam("address") String address) {
        // CHECK IF VALID ADDRESS
        Tuple2<Account, String> result = Account.tryMakeAccount(address);
        if (result.a == null) {
            throw ApiErrorFactory.getInstance().createError(
                    Transaction.INVALID_ADDRESS);

        }

        Account account = result.a;
        ItemAssetBalanceMap map = DCSet.getInstance().getAssetBalanceMap();
        List<Tuple2<byte[], Tuple5<
                Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
                Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>>> assetsBalances
                = map.getBalancesList(account);

        JSONObject out = new JSONObject();

        for (Tuple2<byte[], Tuple5<
                Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
                Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>>
                assetsBalance : assetsBalances) {
            JSONArray array = new JSONArray();
            long assetKey = ItemAssetBalanceMap.getAssetKeyFromKey(assetsBalance.a);

            array.add(setJSONArray(assetsBalance.b.a));
            array.add(setJSONArray(assetsBalance.b.b));
            array.add(setJSONArray(assetsBalance.b.c));
            array.add(setJSONArray(assetsBalance.b.d));
            array.add(setJSONArray(assetsBalance.b.e));
            out.put(assetKey, array);
        }

        if (BlockChain.ERA_COMPU_ALL_UP) {
            long assetKey = AssetCls.ERA_KEY;
            Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> assetsBalance;
            if (!out.containsKey(assetKey)) {
                assetsBalance = account.getBalance(assetKey);
                JSONArray array = new JSONArray();
                array.add(setJSONArray(assetsBalance.a));
                array.add(setJSONArray(assetsBalance.b));
                array.add(setJSONArray(assetsBalance.b));
                array.add(setJSONArray(assetsBalance.b));
                array.add(setJSONArray(assetsBalance.b));
                out.put(assetKey, array);
            }
            assetKey = AssetCls.FEE_KEY;
            if (!out.containsKey(assetKey)) {
                assetsBalance = account.getBalance(assetKey);
                JSONArray array = new JSONArray();
                array.add(setJSONArray(assetsBalance.a));
                array.add(setJSONArray(assetsBalance.b));
                array.add(setJSONArray(assetsBalance.b));
                array.add(setJSONArray(assetsBalance.b));
                array.add(setJSONArray(assetsBalance.b));
                out.put(assetKey, array);
            }
            if (BlockChain.NOVA_ASSETS != null && !BlockChain.NOVA_ASSETS.isEmpty()) {
                for (Tuple3<Long, Long, byte[]> nova : BlockChain.NOVA_ASSETS.values()) {
                    assetKey = nova.a;
                    if (!out.containsKey(assetKey)) {
                        assetsBalance = account.getBalance(assetKey);
                        JSONArray array = new JSONArray();
                        array.add(setJSONArray(assetsBalance.a));
                        array.add(setJSONArray(assetsBalance.b));
                        array.add(setJSONArray(assetsBalance.b));
                        array.add(setJSONArray(assetsBalance.b));
                        array.add(setJSONArray(assetsBalance.b));
                        out.put(assetKey, array);
                    }
                }
            }
        }

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(out.toJSONString())
                .build();
    }

    public static JSONArray setJSONArray(Tuple2 t) {
        JSONArray array = new JSONArray();
        array.add(t.a);
        array.add(t.b);
        return array;
    }

    @GET
    @Path("addresspublickey/{address}")
    public Response getPublicKey(@PathParam("address") String address) {

        // CHECK IF VALID ADDRESS
        if (!PublicKeyAccount.isValidPublicKey(address)) {
            throw ApiErrorFactory.getInstance().createError(
                    Transaction.INVALID_ADDRESS);

        }

        byte[] publicKey = Controller.getInstance().getPublicKeyByAddress(address);

        if (publicKey == null) {
            throw ApiErrorFactory.getInstance().createError(
                    Transaction.UNKNOWN_PUBLIC_KEY_FOR_ENCRYPT);
        } else {
            return Response.status(200)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(Base58.encode(publicKey))
                    .build();
        }
    }


    @GET
    @Path("addressforge/{address}")
    public Response getAddressForge(@PathParam("address") String address) {

        // CHECK IF VALID ADDRESS
        Tuple2<Account, String> result = Account.tryMakeAccount(address);
        Account account = result.a;
        if (account == null) {
            throw ApiErrorFactory.getInstance().createError(
                    result.b);
        }

        JSONObject out = new JSONObject();
        BigDecimal forgingValue = account.getBalanceUSE(Transaction.RIGHTS_KEY, dcSet);
        int height = Controller.getInstance().getMyHeight() + 1;
        long previousTarget = Controller.getInstance().blockChain.getTarget(dcSet);
        // previous making blockHeight + previous ForgingH balance + this ForgingH balance
        Tuple3<Integer, Integer, Integer> lastForgingPoint = account.getLastForgingData(dcSet);
        if (lastForgingPoint == null) {
            out.put("lastForgingPoint", "null");
        } else {
            JSONObject lastForgingPointJSON = new JSONObject();
            lastForgingPointJSON.put("height", lastForgingPoint.a);
            lastForgingPointJSON.put("prevBalance", lastForgingPoint.b);
            lastForgingPointJSON.put("balance", lastForgingPoint.c);
            out.put("lastPoint", lastForgingPointJSON);
            Tuple3<Integer, Integer, Integer> forgingPoint = account.getForgingData(dcSet, lastForgingPoint.a);
            if (forgingPoint == null) {
                out.put("forgingPoint", "null");
            } else {
                JSONObject forgingPointJson = new JSONObject();
                forgingPointJson.put("prevHeight", forgingPoint.a);
                forgingPointJson.put("prevBalance", forgingPoint.b);
                forgingPointJson.put("balance", forgingPoint.c);
                out.put("forgingPoint", forgingPointJson);
            }
        }

        long winValue = BlockChain.calcWinValue(dcSet, account, height, forgingValue.intValue(), lastForgingPoint);
        int targetedWinValue = BlockChain.calcWinValueTargetedBase(dcSet, height, winValue, previousTarget);
        out.put("forgingValue", forgingValue.toPlainString());
        out.put("height", height);
        out.put("winValue", winValue);
        out.put("previousTarget", previousTarget);
        out.put("targetedWinValue", targetedWinValue);

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(out.toJSONString())
                .build();
    }

    @GET
    @Path("addresspersonkey/{address}")
    public Response getPersonKey(@PathParam("address") String address) {

        // CHECK IF VALID ADDRESS
        Tuple2<Account, String> result = Account.tryMakeAccount(address);
        if (result.a == null) {
            throw ApiErrorFactory.getInstance().createError(
                    Transaction.INVALID_ADDRESS);

        }

        Tuple4<Long, Integer, Integer, Integer> personItem = DCSet.getInstance().getAddressPersonMap().getItem(address);

        if (personItem == null) {
            throw ApiErrorFactory.getInstance().createError(
                    Transaction.CREATOR_NOT_PERSONALIZED);
        } else {
            return Response.status(200)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .header("Access-Control-Allow-Origin", "*")
                    .entity("" + personItem.a)
                    .build();
        }
    }

    @GET
    @Path("addressasperson/{address}")
    public Response getAddressAsPerson(@PathParam("address") String address) {

        // CHECK IF VALID ADDRESS
        Tuple2<Account, String> result = Account.tryMakeAccount(address);
        if (result.a == null) {
            throw ApiErrorFactory.getInstance().createError(
                    Transaction.INVALID_ADDRESS);

        }

        Tuple4<Long, Integer, Integer, Integer> personItem = DCSet.getInstance().getAddressPersonMap()
                .getItem(result.a.getShortAddressBytes());

        JSONObject out = new JSONObject();
        if (personItem != null) {
            PersonCls person = (PersonCls) DCSet.getInstance().getItemPersonMap().get(personItem.a);
            out.put("person", person.toJson());
            out.put("endDate", 86400000L * personItem.b); // timestamp
            out.put("seqNo", personItem.c + "-" + personItem.d);

        }

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(out.toJSONString())
                .build();
    }

    @GET
    @Path("addressaspersonlite/{address}")
    public Response getAddressAsPersonLite(@PathParam("address") String address) {

        // CHECK IF VALID ADDRESS
        Tuple2<Account, String> result = Account.tryMakeAccount(address);
        if (result.a == null) {
            throw ApiErrorFactory.getInstance().createError(
                    Transaction.INVALID_ADDRESS);

        }

        Tuple4<Long, Integer, Integer, Integer> personItem = DCSet.getInstance().getAddressPersonMap()
                .getItem(result.a.getShortAddressBytes());

        String outStr;
        JSONObject out = new JSONObject();
        if (personItem == null) {
            outStr = "";
        } else {
            PersonCls person = (PersonCls) DCSet.getInstance().getItemPersonMap().get(personItem.a);
            out.put("name", person.viewName());
            out.put("key", personItem.a);
            outStr = out.toJSONString();
        }

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(outStr)
                .build();
    }

    @GET
    @Path("getaccountsfromperson/{key}")
    public Response getAccountsFromPerson(@PathParam("key") String key) {
        JSONObject out = new JSONObject();
        ItemCls cls = DCSet.getInstance().getItemPersonMap().get(new Long(key));
        if (DCSet.getInstance().getItemPersonMap().get(new Long(key)) == null) {
            out.put("error", "Person not Found");
        } else {
            TreeMap<String, Stack<Tuple3<Integer, Integer, Integer>>> addresses = DCSet.getInstance().getPersonAddressMap().getItems(new Long(key));
            if (addresses.isEmpty()) {
                out.put("null", "null");
            } else {
                Set<String> ad = addresses.keySet();
                int i = 0;
                for (String a : ad) {
                    out.put(i, a);
                    i++;
                }
            }
        }
        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(out.toJSONString())
                .build();
    }

    /*
     * ************* ASSET **************
     */
    @GET
    @Deprecated
    @Path("assetheight")
    public Response assetHeight() {

        long height = dcSet.getItemAssetMap().getLastKey();

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity("" + height)
                .build();

    }

    @GET
    @Deprecated
    @Path("asset/{key}")
    public Response asset(@PathParam("key") long key) {

        ItemAssetMap map = DCSet.getInstance().getItemAssetMap();

        AssetCls asset = (AssetCls) map.get(key);
        if (asset == null) {
            throw ApiErrorFactory.getInstance().createError(
                    Transaction.ITEM_ASSET_NOT_EXIST);
        }
        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(asset.toJson().toJSONString())
                .build();

    }

    @GET
    @Deprecated
    @Path("assetdata/{key}")
    public Response assetData(@PathParam("key") long key) {

        ItemAssetMap map = DCSet.getInstance().getItemAssetMap();

        AssetCls asset = (AssetCls) map.get(key);
        if (asset == null) {
            throw ApiErrorFactory.getInstance().createError(
                    Transaction.ITEM_ASSET_NOT_EXIST);
        }

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(asset.toJsonData().toJSONString())
                .build();

    }

    /*
     * ************* ASSETS **************
     */

    @Deprecated
    @GET
    @Path("assetsfilter/{filter_name_string}")
    public Response assetsFilter(@PathParam("filter_name_string") String filter,
                                 @QueryParam("from") Long fromID,
                                 @QueryParam("offset") int offset,
                                 @QueryParam("limit") int limit) {

        return APIItemAsset.find(filter, null, fromID, offset, limit, true);

    }

    /*
     * ************* EXCHANGE **************
     */
    @GET
    @Path("exchangeorders/{have}/{want}")
    public Response exchangeOrders(@PathParam("have") long have, @PathParam("want") long want) {

        ItemAssetMap map = DCSet.getInstance().getItemAssetMap();
        // DOES ASSETID EXIST
        if (!map.contains(have)) {
            throw ApiErrorFactory.getInstance().createError(
                    Transaction.ITEM_ASSET_NOT_EXIST);
        }
        if (!map.contains(want)) {
            throw ApiErrorFactory.getInstance().createError(
                    Transaction.ITEM_ASSET_NOT_EXIST);
        }

        JSONArray arraySell = new JSONArray();
        List<Order> orders = this.dcSet.getOrderMap().getOrdersForTrade(have, want, false);
        for (Order order : orders) {
            JSONArray itemJson = new JSONArray();
            itemJson.add(order.getAmountHaveLeft());
            itemJson.add(order.calcLeftPrice());
            itemJson.add(order.getAmountWantLeft());

            arraySell.add(itemJson);

        }

        JSONArray arrayBuy = new JSONArray();
        orders = this.dcSet.getOrderMap().getOrdersForTrade(want, have, false);
        for (Order order : orders) {
            JSONArray itemJson = new JSONArray();
            itemJson.add(order.getAmountHaveLeft());
            itemJson.add(order.calcLeftPriceReverse()); // REVERSE
            itemJson.add(order.getAmountWantLeft());

            arrayBuy.add(itemJson);

        }

        JSONObject itemJSON = new JSONObject();

        // ADD DATA
        itemJSON.put("buy", arrayBuy);
        itemJSON.put("sell", arraySell);
        itemJSON.put("pair", have + ":" + want);

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(itemJSON.toJSONString())
                .build();

    }


    /*
     * ************* PERSON **************
     */
    @GET
    @Path("personheight")
    public Response personHeight() {

        long height = dcSet.getItemPersonMap().getLastKey();

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity("" + height)
                .build();

    }

    @GET
    @Path("person/{key}")
    public Response person(@PathParam("key") long key) {

        ItemPersonMap map = DCSet.getInstance().getItemPersonMap();
        // DOES EXIST
        if (!map.contains(key)) {
            throw ApiErrorFactory.getInstance().createError(
                    Transaction.ITEM_PERSON_NOT_EXIST);
        }

        PersonCls person = (PersonCls) map.get(key);

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(person.toJson().toJSONString())
                .build();

    }

    @Path("assetimage/{key}")
    @GET
    @Produces({"image/png", "image/jpeg"})
    public Response assetImage(@PathParam("key") long key) throws IOException {

        int weight = 0;
        if (key <= 0) {
            throw ApiErrorFactory.getInstance().createError(
                    "Error key");
        }

        ItemAssetMap map = DCSet.getInstance().getItemAssetMap();
        // DOES EXIST
        if (!map.contains(key)) {
            throw ApiErrorFactory.getInstance().createError(
                    Transaction.ITEM_ASSET_NOT_EXIST);
        }

        AssetCls asset = (AssetCls) map.get(key);

        if (asset.getImage() != null) {
            return Response.status(200)
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(new ByteArrayInputStream(asset.getImage()))
                    .build();
        }
        return Response.status(200)
                .header("Access-Control-Allow-Origin", "*")
                .entity("")
                .build();

    }

    @Deprecated
    @Path("asseticon/{key}")
    @GET
    @Produces({"image/png", "image/jpeg"})
    public Response assetIcon(@PathParam("key") long key) throws IOException {

        if (key <= 0) {
            throw ApiErrorFactory.getInstance().createError(
                    "Error key");
        }

        ItemAssetMap map = DCSet.getInstance().getItemAssetMap();
        // DOES EXIST
        if (!map.contains(key)) {
            throw ApiErrorFactory.getInstance().createError(
                    Transaction.ITEM_ASSET_NOT_EXIST);
        }

        AssetCls asset = (AssetCls) map.get(key);

        if (asset.getIcon() != null) {
            return Response.status(200)
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(new ByteArrayInputStream(asset.getIcon()))
                    .build();
        }
        return Response.status(200)
                .header("Access-Control-Allow-Origin", "*")
                .entity("")
                .build();
    }

    @Deprecated
    @Path("personimage/{key}")
    @GET
    @Produces({"image/png", "image/jpeg"})
    public Response getFullImage(@PathParam("key") long key) throws IOException {

        int weight = 0;
        if (key <= 0) {
            throw ApiErrorFactory.getInstance().createError(
                    "Error key");
        }

        ItemPersonMap map = DCSet.getInstance().getItemPersonMap();
        // DOES EXIST
        if (!map.contains(key)) {
            throw ApiErrorFactory.getInstance().createError(
                    Transaction.ITEM_PERSON_NOT_EXIST);
        }

        PersonCls person = (PersonCls) map.get(key);

        return Response.status(200)
                .header("Access-Control-Allow-Origin", "*")
                .entity(new ByteArrayInputStream(person.getImage()))
                .build();
    }


    @GET
    @Deprecated
    @Path("persondata/{key}")
    public Response personData(@PathParam("key") long key) {

        ItemPersonMap map = DCSet.getInstance().getItemPersonMap();
        // DOES ASSETID EXIST
        if (!map.contains(key)) {
            throw ApiErrorFactory.getInstance().createError(
                    Transaction.ITEM_PERSON_NOT_EXIST);
        }

        PersonCls person = (PersonCls) map.get(key);

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(person.toJsonData().toJSONString())
                .build();

    }

    @GET
    @Path("personkeybyaddress/{address}")
    public Response getPersonKeyByAddres(@PathParam("address") String address) {

        // CHECK IF VALID ADDRESS
        Tuple2<Account, String> result = Account.tryMakeAccount(address);
        if (result.a == null) {
            throw ApiErrorFactory.getInstance().createError(
                    Transaction.INVALID_ADDRESS);

        }

        Tuple4<Long, Integer, Integer, Integer> personItem = DCSet.getInstance().getAddressPersonMap().getItem(address);

        if (personItem == null) {
            throw ApiErrorFactory.getInstance().createError(
                    Transaction.CREATOR_NOT_PERSONALIZED);
        } else {
            return Response.status(200)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .header("Access-Control-Allow-Origin", "*")
                    .entity("" + personItem.a)
                    .build();
        }
    }

    @GET
    @Path("personkeybypublickey/{publickey}")
    public Response getPersonKeyByPublicKey(@PathParam("publickey") String publicKey) {

        // CHECK IF VALID ADDRESS
        if (!PublicKeyAccount.isValidPublicKey(publicKey)) {
            throw ApiErrorFactory.getInstance().createError(
                    Transaction.INVALID_PUBLIC_KEY);

        }

        PublicKeyAccount publicKeyAccount = new PublicKeyAccount(publicKey);

        Tuple4<Long, Integer, Integer, Integer> personItem = DCSet.getInstance().getAddressPersonMap().getItem(publicKeyAccount.getShortAddressBytes());

        if (personItem == null) {
            throw ApiErrorFactory.getInstance().createError(
                    Transaction.CREATOR_NOT_PERSONALIZED);
        } else {
            return Response.status(200)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .header("Access-Control-Allow-Origin", "*")
                    .entity("" + personItem.a)
                    .build();
        }
    }

    @GET
    @Path("personkeybyownerpublickey/{publickey}")
    public Response getPersonKeyByOwnerPublicKey(@PathParam("publickey") String publicKey) {

        // CHECK IF VALID ADDRESS
        if (!PublicKeyAccount.isValidPublicKey(publicKey)) {
            throw ApiErrorFactory.getInstance().createError(
                    Transaction.INVALID_PUBLIC_KEY);

        }

        PublicKeyAccount publicKeyAccount = new PublicKeyAccount(publicKey);
        byte[] pkBytes = publicKeyAccount.getPublicKey();
        if (!DCSet.getInstance().getTransactionFinalMapSigns().contains(pkBytes)) {
            throw ApiErrorFactory.getInstance().createError(
                    Transaction.ITEM_PERSON_NOT_EXIST);
        } else {
            Long key = DCSet.getInstance().getTransactionFinalMapSigns().get(pkBytes);
            if (key == null || key == 0) {
                throw ApiErrorFactory.getInstance().createError(
                        Transaction.ITEM_PERSON_NOT_EXIST);
            }

            return Response.status(200)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .header("Access-Control-Allow-Origin", "*")
                    .entity("" + key)
                    .build();
        }
    }

    @GET
    @Path("personkeybyownerpublickey32/{publickey}")
    public Response getPersonKeyByOwnerPublicKey32(@PathParam("publickey") String publicKey32) {

        JSONObject answer = new JSONObject();
        try {
            byte[] publicKey = Base32.decode(publicKey32);
            return getPersonKeyByOwnerPublicKey(Base58.encode(publicKey));
        } catch (Exception e) {
            answer.put("Error", "Invalid Base32 Key");
            return Response.status(200)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(answer.toJSONString())
                    .build();
        }
    }


    @GET
    @Path("personbyaddress/{address}")
    public Response personByAddress(@PathParam("address") String address) {

        // CHECK IF VALID ADDRESS
        Tuple2<Account, String> result = Account.tryMakeAccount(address);
        if (result.a == null) {
            throw ApiErrorFactory.getInstance().createError(
                    Transaction.INVALID_ADDRESS);

        }

        Tuple4<Long, Integer, Integer, Integer> personItem = DCSet.getInstance().getAddressPersonMap().getItem(address);

        if (personItem == null) {
            throw ApiErrorFactory.getInstance().createError(
                    Transaction.ITEM_PERSON_NOT_EXIST);
        }

        long key = personItem.a;
        ItemPersonMap map = DCSet.getInstance().getItemPersonMap();
        // DOES EXIST
        if (!map.contains(key)) {
            throw ApiErrorFactory.getInstance().createError(
                    Transaction.ITEM_PERSON_NOT_EXIST);
        }

        PersonCls person = (PersonCls) map.get(key);

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(person.toJson().toJSONString())
                .build();

    }

    @GET
    @Path("personbypublickey/{publickey}")
    public Response personByPublicKey(@PathParam("publickey") String publicKey) {

        // CHECK IF VALID ADDRESS
        if (!PublicKeyAccount.isValidPublicKey(publicKey)) {
            throw ApiErrorFactory.getInstance().createError(
                    Transaction.INVALID_PUBLIC_KEY);

        }

        PublicKeyAccount publicKeyAccount = new PublicKeyAccount(publicKey);

        Tuple4<Long, Integer, Integer, Integer> personItem = DCSet.getInstance().getAddressPersonMap().getItem(publicKeyAccount.getShortAddressBytes());

        if (personItem == null) {
            throw ApiErrorFactory.getInstance().createError(
                    Transaction.ITEM_PERSON_NOT_EXIST);
        }

        long key = personItem.a;
        ItemPersonMap map = DCSet.getInstance().getItemPersonMap();
        // DOES EXIST
        if (!map.contains(key)) {
            throw ApiErrorFactory.getInstance().createError(
                    Transaction.ITEM_PERSON_NOT_EXIST);
        }

        PersonCls person = (PersonCls) map.get(key);

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(person.toJson().toJSONString())
                .build();
    }

    @GET
    @Path("personbyownerpublickey/{publickey}")
    public Response getPersonByOwnerPublicKey(@PathParam("publickey") String publicKey) {

        // CHECK IF VALID ADDRESS
        if (!PublicKeyAccount.isValidPublicKey(publicKey)) {
            throw ApiErrorFactory.getInstance().createError(
                    Transaction.INVALID_PUBLIC_KEY);

        }

        PublicKeyAccount publicKeyAccount = new PublicKeyAccount(publicKey);
        byte[] pkBytes = publicKeyAccount.getPublicKey();
        if (!DCSet.getInstance().getTransactionFinalMapSigns().contains(pkBytes)) {
            throw ApiErrorFactory.getInstance().createError(
                    Transaction.ITEM_PERSON_NOT_EXIST);
        } else {
            Long key = DCSet.getInstance().getTransactionFinalMapSigns().get(pkBytes);
            if (key == null || key == 0) {
                throw ApiErrorFactory.getInstance().createError(
                        Transaction.ITEM_PERSON_NOT_EXIST);
            }

            PersonCls person = (PersonCls) DCSet.getInstance().getItemPersonMap().get(key);
            if (person == null) {
                throw ApiErrorFactory.getInstance().createError(
                        Transaction.ITEM_PERSON_NOT_EXIST);
            }

            return Response.status(200)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(person.toJson().toJSONString())
                    .build();
        }
    }

    @Deprecated
    @GET
    @Path("personbyownerpublickey32/{publickey}")
    public Response getPersonByOwnerPublicKey32(@PathParam("publickey") String publicKey32) {

        JSONObject answer = new JSONObject();
        try {
            byte[] publicKey = Base32.decode(publicKey32);
            return getPersonByOwnerPublicKey(Base58.encode(publicKey));
        } catch (Exception e) {
            answer.put("Error", "Invalid Base32 Key");
            return Response.status(200)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(answer.toJSONString())
                    .build();
        }
    }

    @GET
    @Path("personbypublickeybase32/{publickeybase}")
    @Deprecated
    public Response personsByBankKey(@PathParam("publickeybase") String publicKey32) {

        return getPersonByOwnerPublicKey32(publicKey32);

    }

    /*
     * ************* PERSONS **************
     */

    @GET
    @Path("personsfilter/{filter_name_string}")
    public Response personsFilter(@PathParam("filter_name_string") String filter,
                                  @QueryParam("from") Long fromID,
                                  @QueryParam("offset") int offset,
                                  @QueryParam("limit") int limit) {

        if (limit > 100) {
            limit = 100;
        }

        if (filter == null || filter.length() < 3) {
            return Response.status(501)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .header("Access-Control-Allow-Origin", "*")
                    .entity("error - so small filter length")
                    .build();
        }

        ItemPersonMap map = DCSet.getInstance().getItemPersonMap();
        // DOES ASSETID EXIST
        List<ItemCls> list = map.getByFilterAsArray(filter, fromID, offset, limit, true);

        JSONArray array = new JSONArray();

        if (list != null) {
            for (ItemCls item : list) {
                array.add(item.toJson());
            }
        }

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(array.toJSONString())
                .build();

    }


    /*
     * ************* TOOLS **************
     */

    /**
     * wiury2876rw7yer8923y63riyrf9287y6r87wyr9737yriwuyr3yr978ry48732y3rsiouyvbkshefiuweyriuwer
     * {"trtr": 293847}
     * @param x
     * @return
     */
    @POST
    @Path("verifysignature")
    public Response verifysignature(String x) {
        try {
            // READ JSON
            JSONObject jsonObject = (JSONObject) JSONValue.parse(x);
            String message = (String) jsonObject.get("message");
            String signature = (String) jsonObject.get("signature");
            String publicKey = (String) jsonObject.get("publickey");

            // DECODE SIGNATURE
            byte[] signatureBytes;
            try {
                signatureBytes = Base58.decode(signature);
            } catch (Exception e) {
                throw ApiErrorFactory.getInstance().createError(
                        Transaction.INVALID_SIGNATURE);

            }

            // DECODE PUBLICKEY
            byte[] publicKeyBytes;
            try {
                publicKeyBytes = Base58.decode(publicKey);
            } catch (Exception e) {
                throw ApiErrorFactory.getInstance().createError(
                        Transaction.INVALID_PUBLIC_KEY);

            }

            return Response.status(200)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(String.valueOf(Crypto.getInstance().verify(publicKeyBytes,
                            signatureBytes, message.getBytes(StandardCharsets.UTF_8))))
                    .build();

        } catch (NullPointerException e) {
            // JSON EXCEPTION
            throw ApiErrorFactory.getInstance().createError(
                    ApiErrorFactory.ERROR_JSON);
        } catch (ClassCastException e) {
            // JSON EXCEPTION
            throw ApiErrorFactory.getInstance().createError(
                    ApiErrorFactory.ERROR_JSON);
        }
    }

    @GET
    @Path("info")
    public Response getInformation() throws NoSuchFieldException, IllegalAccessException {
        JSONObject jsonObject = CoreResource.infoJson();

        if (false) {
            Object f = Controller.class.getDeclaredField("version");
            ((Field) f).setAccessible(true);
            String version = ((Field) f).get(Controller.getInstance()).toString();
            ((Field) f).setAccessible(false);
            jsonObject.put("version2", version);
        }

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(jsonObject.toJSONString())
                .build();
    }

    @GET
    @Path("bench")
    public Response getSpeedInfo() {
        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(Controller.getInstance().getBenchmarks().toJSONString())
                .build();
    }

    public static boolean checkBoolean(UriInfo info, String param) {
        String value = info.getQueryParameters().getFirst(param);
        if (value == null) {
            return false;
        } else {
            return value.isEmpty() || new Boolean(value);
        }
    }
}
