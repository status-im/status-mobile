(ns utils.image-server-test
  (:require [cljs.test :as t]
            status-im2.common.pixel-ratio
            [utils.image-server :as sut]))

(t/deftest get-account-image-uri
  (with-redefs
    [sut/current-theme-index             identity
     status-im2.common.pixel-ratio/ratio 2
     sut/timestamp                       (constantly "timestamp")]
    (t/is
     (=
      (sut/get-account-image-uri {:port                     "port"
                                  :public-key               "public-key"
                                  :image-name               "image-name"
                                  :key-uid                  "key-uid"
                                  :theme                    :dark
                                  :indicator-size           2
                                  :indicator-color          "rgba(9,16,28,0.08)"
                                  :indicator-center-to-edge 6
                                  :ring?                    true
                                  :ring-width               2})
      "https://localhost:port/accountImages?publicKey=public-key&keyUid=key-uid&imageName=image-name&size=0&theme=:dark&clock=timestamp&indicatorColor=rgba(9%2C16%2C28%2C0.08)&indicatorSize=4&indicatorBorder=0&indicatorCenterToEdge=12&addRing=1&ringWidth=4"))))

(t/deftest get-account-initials-uri
  (with-redefs
    [sut/current-theme-index             identity
     status-im2.common.pixel-ratio/ratio 2
     sut/timestamp                       (constantly "timestamp")]
    (t/is
     (=
      (sut/get-initials-avatar-uri
       {:port                     "port"
        :public-key               "public-key"
        :key-uid                  "key-uid"
        :full-name                "full-name"
        :length                   "length"
        :size                     48
        :theme                    :light
        :ring?                    "ring?"
        :background-color         "background-color"
        :color                    "#0E162000"
        :font-size                12
        :font-file                "/font/Inter Medium.otf"
        :uppercase-ratio          "uppercase-ratio"
        :indicator-size           2
        :indicator-center-to-edge 6
        :indicator-color          "#0E1620"
        :ring-width               4})
      "https://localhost:port/accountInitials?publicKey=public-key&keyUid=key-uid&length=length&size=96&bgColor=background-color&color=%230E162000&fontSize=24&fontFile=%2Ffont%2FInter%20Medium.otf&uppercaseRatio=uppercase-ratio&theme=:light&clock=&name=full-nametimestamp&indicatorColor=%230E1620&indicatorSize=4&indicatorBorder=0&indicatorCenterToEdge=12&addRing=1&ringWidth=8"))))
