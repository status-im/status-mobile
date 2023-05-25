(ns utils.image-server-test
  (:require [cljs.test :as t]
            [utils.image-server :as sut]))

(t/deftest get-account-image-uri
  (with-redefs
    [sut/current-theme-index identity
     sut/timestamp           (constantly "timestamp")]
    (t/is
     (=
      (sut/get-account-image-uri {:port       "port"
                                  :public-key "public-key"
                                  :image-name "image-name"
                                  :key-uid    "key-uid"
                                  :theme      "theme"
                                  :ring?      true})
      "https://localhost:port/accountImages?publicKey=public-key&keyUid=key-uid&imageName=image-name&theme=theme&clock=timestamp&addRing=1"))))
