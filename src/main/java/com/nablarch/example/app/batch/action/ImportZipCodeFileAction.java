package com.nablarch.example.app.batch.action;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import com.nablarch.example.app.batch.form.ZipCodeForm;
import com.nablarch.example.app.batch.interceptor.ValidateData;
import com.nablarch.example.app.batch.reader.ZipCodeFileReader;
import com.nablarch.example.app.entity.ZipCodeData;

import lerna.nablarch.batch.parallelizable.handler.ControllableParallelExecutor;
import nablarch.common.dao.UniversalDao;
import nablarch.core.beans.BeanUtil;
import nablarch.core.util.annotation.Published;
import nablarch.fw.DataReader;
import nablarch.fw.ExecutionContext;
import nablarch.fw.Result;
import nablarch.fw.action.BatchAction;

/**
 * 住所ファイルをDBに登録するバッチクラス。
 */
@Published
public class ImportZipCodeFileAction extends BatchAction<ZipCodeForm> implements ControllableParallelExecutor<ZipCodeForm> {

    /**
     * {@link ZipCodeFileReader} から渡された一行分の情報をDBに登録する。
     * <p/>
     * メソッド実行時に{@link ValidateData} がインターセプトされるため、
     * このメソッドには常にバリデーション済みの {@code inputData} が引き渡される。
     *
     * @param inputData 一行分の住所情報
     * @param ctx       実行コンテキスト
     * @return 結果オブジェクト
     */
    @Override
    @ValidateData
    public Result handle(ZipCodeForm inputData, ExecutionContext ctx) {

        //処理区分による分岐
        if (inputData.getActionCode().equals("0")) {
            //新規登録
            ZipCodeData data = BeanUtil.createAndCopy(ZipCodeData.class, inputData);
            UniversalDao.insert(data);

        } else {
            //更新登録
            ZipCodeData condition = new ZipCodeData();
            condition.setZipCode7digit(inputData.getZipCode7digit());

            //登録済レコード取得
            ZipCodeData data = UniversalDao.findBySqlFile(ZipCodeData.class, "FIND_BY_ZIP_CODE_7", condition);

            //キーペア生成
            KeyPair keyPair = generateKeyPair(data.getZipCode7digit());

            //キーペア登録
            data.setPrivateKey(keyPair.getPrivate().getFormat() + ":"
                    + Base64.getMimeEncoder().encodeToString(keyPair.getPrivate().getEncoded()));
            data.setPublicKey(Base64.getMimeEncoder().encodeToString(keyPair.getPublic().getEncoded()));

            //更新
            UniversalDao.update(data);

        }

        return new Result.Success();
    }

    /**
     * キーペアを生成する
     * @param seed シード値
     * @return キーペア
     */
    private KeyPair generateKeyPair(String seed) {

        KeyPair pair = null;
        for (int i = 0; i < 10; i++) {
            KeyPairGenerator kpGen = null;
            try {
                kpGen = KeyPairGenerator.getInstance("RSA");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
            kpGen.initialize(1024, new SecureRandom(seed.getBytes()));
            pair = kpGen.generateKeyPair();
        }
        return pair;
    }

    /**
     * リーダを作成する。
     *
     * @param ctx 実行コンテキスト
     * @return リーダーオブジェクト
     */
    @Override
    public DataReader<ZipCodeForm> createReader(ExecutionContext ctx) {
        return new ZipCodeFileReader();
    }

    @Override
    public SequentialExecutionIdExtractor sequentialExecutionId(ZipCodeForm element) {
        return SequentialExecution.byHashCodes(element.getZipCode7digit());
    }
}
