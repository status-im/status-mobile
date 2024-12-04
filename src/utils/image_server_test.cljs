(ns utils.image-server-test
  (:require
    [cljs.test :as t]
    [quo.foundations.colors :as colors]
    [utils.image-server :as sut]))

(t/deftest get-account-image-uri-test
  (with-redefs
    [sut/current-theme-index identity]
    (t/is
     (=
      (sut/get-account-image-uri {:port                     "port"
                                  :public-key               "public-key"
                                  :ratio                    2
                                  :image-name               "image-name"
                                  :key-uid                  "key-uid"
                                  :clock                    "timestamp"
                                  :theme                    :dark
                                  :indicator-size           2
                                  :indicator-color          "rgba(9,16,28,0.08)"
                                  :indicator-center-to-edge 6
                                  :ring?                    true
                                  :ring-width               2})
      "https://localhost:port/accountImages?publicKey=public-key&keyUid=key-uid&imageName=image-name&size=0&theme=:dark&clock=timestamp&indicatorColor=rgba(9%2C16%2C28%2C0.08)&indicatorSize=4&indicatorBorder=0&indicatorCenterToEdge=12&addRing=1&ringWidth=4"))))

(t/deftest get-account-initials-uri-test
  (with-redefs
    [sut/current-theme-index identity
     colors/resolve-color    str]
    (t/is
     (=
      (sut/get-initials-avatar-uri
       {:port                     "port"
        :public-key               "public-key"
        :ratio                    2
        :key-uid                  "key-uid"
        :clock                    "timestamp"
        :full-name                "full-name"
        :length                   "length"
        :size                     48
        :theme                    :light
        :ring?                    "ring?"
        :customization-color      :blue
        :color                    "#0E162000"
        :font-size                12
        :font-file                "/font/Inter Medium.otf"
        :uppercase-ratio          0.6
        :indicator-size           2
        :indicator-center-to-edge 6
        :indicator-color          "#0E1620"
        :ring-width               4})
      "https://localhost:port/accountInitials?publicKey=public-key&keyUid=key-uid&length=length&size=96&bgColor=%3Ablue%3Alight&color=%230E162000&fontSize=24&fontFile=%2Ffont%2FInter%20Medium.otf&uppercaseRatio=0.6&theme=:light&clock=timestamp&name=full-name&indicatorColor=%230E1620&indicatorSize=4&indicatorBorder=0&indicatorCenterToEdge=12&addRing=1&ringWidth=8"))))
