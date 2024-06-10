(ns status-im.subs.profile-test
  (:require [cljs.test :refer [is testing use-fixtures]]
            [re-frame.db :as rf-db]
            status-im.subs.root
            [test-helpers.unit :as h]
            [utils.re-frame :as rf]))

(use-fixtures :each
              {:before #(reset! rf-db/app-db {})})

(def sample-profile
  {:keycard-pairing nil
   :send-push-notifications? true
   :send-status-updates? true
   :key-uid "0x2285f5c1ffd94ade0aa3568bff85f6c06f2860391ba65ccf56276cbc6829a22a"
   :backup-enabled? true
   :address "0x70F8913fbE0Ca5687F1Fb73068944d6e99B27804"
   :mnemonic "lucky veteran business source debris large priority color endless answer strong pave"
   :preview-privacy? true
   :identicon ""
   :use-mailservers? true
   :signing-phrase "polo rush vest"
   :url-unfurling-mode 1
   :custom-bootnodes-enabled? {}
   :log-level "INFO"
   :profile-pictures-visibility 2
   :messages-from-contacts-only false
   :pinned-mailservers {}
   :eip1581-address "0x15636c0aa4036b9f984e8998db085328795b26d8"
   :images [{:keyUid       "0x2285f5c1ffd94ade0aa3568bff85f6c06f2860391ba65ccf56276cbc6829a22a"
             :type         "large"
             :uri          "data:image/jpeg;base64,/9j/2wCEAAgGBgcGBQgHBwcJC="
             :width        240
             :height       240
             :fileSize     15973
             :resizeTarget 240
             :clock        0}
            {:keyUid       "0x2285f5c1ffd94ade0aa3568bff85f6c06f2860391ba65ccf56276cbc6829a22a"
             :type         "thumbnail"
             :uri          "data:image/jpeg;base64,/9j/2wCEAAgGBgcGBQgHBwcJC="
             :width        80
             :height       240
             :fileSize     2558
             :resizeTarget 80
             :clock        0}]
   :name "Plush Shiny Songbird"
   :latest-derived-path 0
   :compressed-key "zQ3shS6tp3NsQT4RSUFtnTqnBQzC5kt2SZzxZmnPEiNkHetwj"
   :wallet-legacy/visible-tokens {:mainnet #{:SNT}}
   :kdfIterations 3200
   :ens-name? false
   :emoji-hash ["👮" "🧑🏿‍🏭" "📬" "👰‍♀️" "🦚" "💳" "👨🏿‍🍳" "☝️" "🤰🏾" "🍊" "☁️" "☔" "👷🏽" "🤹🏾"]
   :wallet-root-address "0x704c9a261b918cb8e522f7fc2bc477c12d0c74ac"
   :last-backup 1701832050
   :link-previews-enabled-sites #{}
   :wakuv2-config {:Port                   0
                   :DataDir                ""
                   :LightClient            true
                   :AutoUpdate             true
                   :MaxMessageSize         0
                   :KeepAliveInterval      0
                   :Nameserver             ""
                   :UseShardAsDefaultTopic false
                   :PeerExchange           true
                   :StoreCapacity          0
                   :UDPPort                0
                   :EnableStore            false
                   :EnableFilterFullNode   false
                   :Enabled                true
                   :EnableConfirmations    false
                   :Host                   "0.0.0.0"
                   :CustomNodes            {}
                   :FullNode               false
                   :EnableDiscV5           true
                   :DiscoveryLimit         20
                   :StoreSeconds           0}
   :current-user-visibility-status {:clock       1701798568
                                    :text        ""
                                    :status-type 1}
   :gifs/api-key ""
   :currency :usd
   :gifs/favorite-gifs nil
   :customization-color :magenta
   :default-sync-period 777600
   :photo-path ""
   :dapps-address "0x52fB56556A039244CED121AFB9ec829788Db78c8"
   :custom-bootnodes {}
   :display-name "Alisher Y"
   :gifs/recent-gifs nil
   :appearance 0
   :link-preview-request-enabled true
   :profile-pictures-show-to 2
   :timestamp 1701798892
   :device-name ""
   :colorId 2
   :networks/current-network "mainnet_rpc"
   :mutual-contact-enabled? false
   :public-key
   "0x0445b4d3a20f9fcf95b9e669857f83a073e7fdb7b79d0ac03ffb601d6889c413fa86282a2b2bed46ecf7d499807c1567549367a4eaa2b7b925067d44562d93cfa6"
   :colorHash [[3 25] [4 3] [5 4] [2 0] [1 10] [5 2] [2 4] [1 17] [3 23] [2 19] [4 1]]
   :installation-id "cee7e269-1ca7-4468-a1dd-e60e5cfb0894"})

(h/deftest-sub :profile/currency
  [sub-name]
  (testing "returns the selected currency of user"
    (swap! rf-db/app-db #(assoc % :profile/profile sample-profile))
    (is (match? :usd (rf/sub [sub-name])))))

(h/deftest-sub :profile/currency-symbol
  [sub-name]
  (testing "returns the symbol of the user's selected currency"
    (swap! rf-db/app-db #(assoc % :profile/profile sample-profile))
    (is (match? "$" (rf/sub [sub-name])))))

(h/deftest-sub :profile/public-key
  [sub-name]
  (testing "returns the user's public key"
    (swap! rf-db/app-db #(assoc % :profile/profile sample-profile))
    (is (match? (:public-key sample-profile) (rf/sub [sub-name])))))
