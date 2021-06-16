-- 郵便番号をキーにデータを取得する
-- SQL_ID:GET_XXXX_INFO
FIND_BY_ZIP_CODE_7 =
select
   *
from
   zip_code_data
where
   zip_code_7digit = :zipCode7digit