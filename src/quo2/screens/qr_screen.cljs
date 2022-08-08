(ns quo2.screens.qr-screen
  (:require [quo.react-native :as rn]
            [status-im.i18n.i18n :as i18n]
            [status-im.ui.components.react :as react]
            [status-im.react-native.resources :as resources]
            [status-im.ui.components.qr-code-viewer.views :as qr-code-viewer]
            [status-im.ui.components.icons.icons :as icons]
            [quo2.components.qr :as quo2]
            [re-frame.core :as re-frame]
            [quo2.foundations.colors :as colors]
            [status-im.ui.components.copyable-text :as copyable-text]))

;This is the space where we show various selector options to use the QR code component
;I believe following are the options
;
;-> text input for link address
;-> drop down selector to see type of QR code
;with the following options :
;1) profile address
;2) wallet legacy
;3) wallet multichain

;(defn cool-preview []
;  (let [state  (reagent/atom {:link-to-qr static-qr-code-url})
;        link-to-qr  (reagent/cursor state [:link-to-qr])
;        qr-view-type (reagent/cursor state [:qr-view-type])]
;    (fn []
;      [rn/view {:margin-bottom 50
;                :padding       16}
;       [rn/view {:flex 1}
;        [preview/customizer state descriptor]]
;       [rn/view {:padding-vertical 60
;                 :flex-direction   :row
;                 :justify-content  :center}
;        [quo2/button (merge (dissoc @state
;                                    :theme :before :after)
;                            {:on-press #(println "Hello world!")}
;                            (when @above
;                                  {:above :main-icons2/placeholder})
;                            (when @before
;                                  {:before :main-icons2/placeholder})
;                            (when @after
;                                  {:after :main-icons2/placeholder}))
;         (if @icon :main-icons2/placeholder @label)]]])))


;the idea here is to make a list of resources and their urls which would go
;to the resources folder and for this screen all the icons and assets would come from this
;map of resources
(defn qr-resources []
    {"Etherium" {:url "https://s3-alpha-sig.figma.com/img/8ab2/37c4/8f2b8f259003612f956d3448d4d22264?Expires=1661126400&Signature=DObJFoz5rwgED4MoIcgzEUkYDp50A5d3VnkR5Amy2EZt~fbcabySPqxSpAVp0axvVQqiGaBl2wTUyt757BKbfK8JH0Nu~nAc-gYzujzzJPAvgyMEYb8o7568fqQxYnYwGORDduhWZX7WIsZksIQdd~RL4Npmyzo8HpMmNXeCMXjJW8UdptPfmUlk5tIjA8XvHqnWZYxJ8Jm8XkhfnX-fFa1AArCVWElisJDa5YHm07aNEsGFD-wh5JTaLKcoM7jdPpcmUnj-UMy4JfzsuSkGaSy-403Np65iraGAxGmn6wCFxdV4eLVcME6jyagEQqcgsYAWyRQkfY-hBx28fouiqg__&Key-Pair-Id=APKAINTVSUGEWH5XD5UA"
                 :name "Etherium"
                 :width 40
                 }
     "Arbitrum" {:url "https://s3-alpha-sig.figma.com/img/a493/c328/b9b6cb2db1f451513d705938f8cfb89e?Expires=1661126400&Signature=dmSMuhKKkImEWSw9-czU2XGdi82xD6RKHxkv3yobUQ5ibRQQ47s7Ca67Zt--EBhvAi2TSoz2MYNF7xAQqZp9F3ZUCQWd-UgcMCzrJWamR~KrYKSygNg8icUcKUjSPSlPsZD5Egfm3SinSMSoo0-y24SIR1XEmieGyG5E0JIJoSrYZE8SLIEJUzp9xNTXSjEUrwBi3Bojfte1EGIIy5uuiz0QLIjNPCCTe4IsKEE7aHm1zOwW~IW4qvhMwqkxDdnaj9~8ZP6hZgQ2JESnpq5DLmlVfBnWQ9xK88Im12MxejC7YIPnPvHFU8WduQjb8~X9Cs1NqN46wcntE4XW3RjJsQ__&Key-Pair-Id=APKAINTVSUGEWH5XD5UA"
                 :name "Arbitrum"
                 :width 40
                 }
     "Hermez" {:url "https://s3-alpha-sig.figma.com/img/8ef2/0db3/e2582832749a9ddfbc5729a7b107957f?Expires=1661126400&Signature=K8-N5ZBhBzDz0qx9ovs3eJW4xpRSicX3W1Kq8Qk2HzArEn1Xi3UJdX~t67565samTC~AY1~30-KZlZZISsiQnF-Tl0AjA1J0XBSKMjk7DNuhnKkeoHLkGyCz8MD~Q9UfR8aKklFkXCTY0EekvwyXkDKTgtY6c2hP3Oy10Qpyt~HyTgvQ9qweO-hg14B4V44iRDuzYwj-cWwlH5Cu1wBCZrfO4AdKnoJJnnzUE~m2Z0eogQfY7qz-LQJ2envzFGmCXS38lZg3~MIjy0izIfoDq1LiaaUcYgP~S~gVjHv2KyI2YrvyTcsVw-sPQGLhBOkvhSG2tKbYISHnvPXIzmU6PA__&Key-Pair-Id=APKAINTVSUGEWH5XD5UA"
                 :name "Hermez"
                 :width 40
                 }
     "Optimism" {:url "https://s3-alpha-sig.figma.com/img/371f/dbf2/07b948fd03e4397e4bbb687978b385cb?Expires=1661126400&Signature=aSF2EcWzwDgdJks13xAAT3TNkUF6vYD5G0VQG8ZuWvRhK0hD0o4N6t0IjWKZGxW1EOwv0ERpAV2MxcuIz32wx9WCpG3loKL-hTvMDjTJ7F6aiER9e5jZlNUdqgzsn6Sm1pIIl8~O35NHsW-qtwZTDoPoH2~TJ3ozI6UbMwHnrtoCwzkkrWxL2MxtIWG-Y8j8sFilhVAgZpwgbpgLAere2PI3iQoRYAG18psM3wI7vTvA-PcSdqICh0OUPPEQCb7miL5Hy10UiPX2Huv3Gv8DQmOIIrjAfOau-0UPZYpUcS9Oh0fH78jNN~bpeJlazqYmLNd9SdPHoYw7mcbWxLmaFw__&Key-Pair-Id=APKAINTVSUGEWH5XD5UA"
                 :name "Optimism"
                 :width 40
                 }
     "zkSync" {:url "https://s3-alpha-sig.figma.com/img/6a3d/8678/cb17273e2fbc1f49f040df9f15425fd1?Expires=1661126400&Signature=TIBuqx87m5QSduVzTVqPPHprUL0lKQZlAiY~0auefKx5w7VXJdoBrzlSBXDzi9j3q5tRszJxG1whudt7ZH4Xou8lx36Rb3NoYfEQ0rPkDVgRqL3DmjStS5Hk5dScRTOYoDH6ntZI1uUCGnXccOrDliI9esbNkPHm~9KtCZVNKEVQjBr~dJCFvksOCBvIkD5v0GrJsRfM9r8xRW0z7cOkIcMljPgJ2mVI9PGtzIYGnlgwTdcUU7Loopp7yKA7s1dn-ExQ2Nrf39DOOcCISeNhuNwodyaxn-GuetaL0fviL0JEPedED3IW5FJaYG6rUVJYkHBkP2OFMU8bh4ef6tRjpg__&Key-Pair-Id=APKAINTVSUGEWH5XD5UA"
                 :name "zkSync"
                 :width 40
                 }
     }
  )


(defn preview-qr []
;  this works as expected
;  [quo2/qr {:type :profile
;            :url "https://status.im"
;            :profile "status.app/u/zQ34e2ahd1835eqacc17f6asas12adjie8"}]

  [quo2/qr {:type :legacy
            :url "https://status.im"
            :wallet-address "0x04a8b2fe1c6388a030f3190bc6f3dc3650f324d049fe459d3d4b1ebab79491ad1f3e84f3190b98d09a6facaebdf8b18f51201219c116c72ea10704470ea9006995"
            :multichain-wallet-address "0x04a8b2fe1c6388a030f3190bc6f3dc3650f324d049fe459d3d4b1ebab79491ad1f3e84f3190b98d09a6facaebdf8b18f51201219c116c72ea10704470ea9006995"
            :multi-chain-info (list
                               {:network-type "eth"
                                :name "Ethereum"
                                :network-text-color "rgba(76, 180, 239, 1)"
                                :network-icon-image (resources/get-image :ethereum)}
                               {:network-type "arb"
                                :name "Arbitrum"
                                :network-text-color "rgba(63, 174, 249, 1)"
                                :network-icon-image (resources/get-image :arbitrum)}
                               {:network-type "her"
                                :name "Hermez"
                                :network-text-color "rgba(251, 143, 97, 1)"
                                :network-icon-image (resources/get-image :hermez)}
                               {:network-type "opt"
                                :name "Optimism"
                                :network-text-color "rgba(230, 95, 92, 1)"
                                :network-icon-image (resources/get-image :optimism)}
                               {:network-type "zks"
                                :name "zkSync"
                                :network-text-color "rgba(139, 141, 250, 1)"
                                :network-icon-image (resources/get-image :zksync)})
            :multichain-wallet-address-with-network-chain "eth:arb:her:opt:zks:0x04a8b2fe1c6388a030f3190bc6f3dc3650f324d049fe459d3d4b1ebab79491ad1f3e84f3190b98d09a6facaebdf8b18f51201219c116c72ea10704470ea9006995"
            }]

;  [rn/view {:background-color (:ui-background @colors/theme)
;            :flex             1}
;   [rn/flat-list {:flex                      1
;                  :keyboardShouldPersistTaps :always
;                  :header                    [cool-preview]
;                  :key-fn                    str}]]
)
