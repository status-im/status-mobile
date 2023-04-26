(ns utils.validators-test
  (:require [cljs.test :refer-macros [deftest testing is]]
            [utils.validators :refer [valid-compressed-key?]]))

(deftest test-valid-compressed-key
  (testing "valid"
    (is (valid-compressed-key?
         "zQ3shWj4WaBdf2zYKCkXe6PHxDxNTzZyid1i75879Ue9cX9gA")))
  (testing "uncompressed key"
    (is
     (not
      (valid-compressed-key?
       "0x048a6773339d11ccf5fd81677b7e54daeec544a1287bd92b725047ad6faa9a9b9f8ea86ed5a226d2a994f5f46d0b43321fd8de7b7997a166e67905c8c73cd37ce"))))
  (testing "nil"
    (is (not (valid-compressed-key? nil))))
  (testing "empty string"
    (is (not (valid-compressed-key? ""))))
  (testing "too short"
    (is (not (valid-compressed-key? "zQ3FGR5y6U5a6"))))
  (testing "too long"
    (is (not (valid-compressed-key?
              "zQ3shWj4WaBdf2zYKCkXe6PHxDxNTzZyid1i75879Ue9cX9gA2"))))
  (testing "0x prefix"
    (is (not (valid-compressed-key? "0xFGR5y6U5a6"))))
  (testing "contains I"
    (is (not (valid-compressed-key?
              "zQ3shWj4WaBdf2zYKCkXe6PHxDxNTzZyid1i75879Ue9cX9gI"))))
  (testing "contains O"
    (is (not (valid-compressed-key?
              "zQ3shWj4WaBdf2zYKCkXe6PHxDxNTzZyid1i75879Ue9cX9gO"))))
  (testing "contains l"
    (is (not (valid-compressed-key?
              "zQ3shWj4WaBdf2zYKCkXe6PHxDxNTzZyid1i75879Ue9cX9gl")))))
