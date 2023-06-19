(ns utils.image-server-test
  (:require [cljs.test :as t]
            [utils.image-server :as sut]))

(t/deftest get-account-image-uri
  (with-redefs
    [sut/current-theme-index identity
     sut/timestamp           (constantly "timestamp")]
    (t/is
     (=
      (sut/get-account-image-uri {:port            "port"
                                  :public-key      "public-key"
                                  :image-name      "image-name"
                                  :key-uid         "key-uid"
                                  :theme           :dark
                                  :indicator-size  "indicator-size"
                                  :indicator-color "rgba(9,16,28,0.08)"
                                  :ring?           true})
      "https://localhost:port/accountImages?publicKey=public-key&keyUid=key-uid&imageName=image-name&size=&theme=:dark&clock=timestamp&indicatorColor=rgba(9%2C16%2C28%2C0.08)&indicatorSize=indicator-size&indicatorBorder=&addRing=1"))))

(t/deftest get-account-initials-uri
  (with-redefs
    [sut/current-theme-index identity
     sut/timestamp           (constantly "timestamp")]
    (t/is
     (=
      (sut/get-initials-avatar-uri
       {:port             "port"
        :public-key       "public-key"
        :key-uid          "key-uid"
        :full-name        "full-name"
        :length           "length"
        :size             "size"
        :theme            :light
        :ring?            "ring?"
        :background-color "background-color"
        :color            "#0E162000"
        :font-size        "font-size"
        :font-file        "/font/Inter Medium.otf"
        :uppercase-ratio  "uppercase-ratio"
        :indicator-size   "indicator-size"
        :indicator-color  "#0E1620"})
      "https://localhost:port/accountInitials?publicKey=public-key&keyUid=key-uid&length=length&size=size&bgColor=background-color&color=%230E162000&fontSize=font-size&fontFile=%2Ffont%2FInter%20Medium.otf&uppercaseRatio=uppercase-ratio&theme=:light&clock=&name=full-nametimestamp&indicatorColor=%230E1620&indicatorSize=indicator-size&indicatorBorder=&addRing=1"))))
