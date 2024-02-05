(ns status-im.contexts.preview.quo.share.share-qr-code
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im.common.resources :as resources]
    [status-im.contexts.preview.quo.preview :as preview]
    [utils.image-server :as image-server]
    [utils.re-frame :as rf]))

(def descriptor
  [{:key   :qr-data
    :label "QR data:"
    :type  :text}
   {:key     :type
    :type    :select
    :options [{:key :profile}
              {:key :wallet}
              {:key :saved-address}
              {:key :watched-address}]}])

(def possible-networks [:ethereum :optimism :arbitrum :myNet])

(def networks-selector
  {:key     :networks
   :type    :select
   :options [{:key   (take 1 possible-networks)
              :value "Ethereum"}
             {:key   (take 2 possible-networks)
              :value "Ethereum and Optimism"}
             {:key   (take 3 possible-networks)
              :value "Ethereum, Optimism and Arbitrum"}
             {:key   (take 4 possible-networks)
              :value "Ethereum, Optimism, Arbitrum and unknown"}]})

(def profile-descriptor
  [{:key     :profile-picture
    :type    :select
    :options [{:key   (resources/get-mock-image :user-picture-female2)
               :value "User 1"}
              {:key   (resources/get-mock-image :user-picture-male4)
               :value "User 2"}]}
   {:key  :full-name
    :type :text}
   (preview/customization-color-option)])

(def wallet-descriptor
  [{:key :address
    :type :select
    :options [{:key :legacy}
              {:key :multichain}]}
   {:key     :emoji
    :type    :select
    :options [{:key "🐈"}
              {:key "👻"}
              {:key "🐧"}]}
   networks-selector
   (preview/customization-color-option)])

(def saved-address-descriptor
  [{:key :address
    :type :select
    :options [{:key :legacy}
              {:key :multichain}]}
   {:key     :wallet-user-avatar
    :type    :text}
   networks-selector
   (preview/customization-color-option)])

(def watched-address-descriptor
  [{:key :address
    :type :select
    :options [{:key :legacy}
              {:key :multichain}]}
   {:key     :emoji
    :type    :select
    :options [{:key "🐈"}
              {:key "👻"}
              {:key "🐧"}]}
   networks-selector
   (preview/customization-color-option)])

;; (def wallet-legacy-descriptor
;;   [{:key     :emoji
;;     :type    :select
;;     :options [{:key "🐈"}
;;               {:key "👻"}
;;               {:key "🐧"}]}
;;    {:key  :watched-account?
;;     :type :boolean}
;;    (preview/customization-color-option)])

;; (def wallet-multichain-descriptor
;;   [{:key     :emoji
;;     :type    :select
;;     :options [{:key "🐈"}
;;               {:key "👻"}
;;               {:key "🐧"}]}
;;    {:key  :watched-account?
;;     :type :boolean}
;;    (preview/customization-color-option)
;;    {:key     :networks
;;     :type    :select
;;     :options [{:key   (take 1 possible-networks)
;;                :value "Ethereum"}
;;               {:key   (take 2 possible-networks)
;;                :value "Ethereum and Optimism"}
;;               {:key   (take 3 possible-networks)
;;                :value "Ethereum, Optimism and Arbitrum"}
;;               {:key   (take 4 possible-networks)
;;                :value "Ethereum, Optimism, Arbitrum and unknown"}]}])

(defn- get-network-short-name-url
  [network]
  (case network
    :ethereum "eth:"
    :optimism "opt:"
    :arbitrum "arb1:"
    (str (name network) ":")))

(def ^:private profile-link
  "https://status.app/u/CwWACgkKB0VlZWVlZWUD#zQ3shUeRSwU6rnUk5JfK2k5HRiM5Hy3wU3UZQrKVzopmAHcQv")

(def ^:private wallet-address "0x39cf6E0Ba4C4530735616e1Ee7ff5FbCB726fBd2")

(defn- set-qr-data-based-on-type
  [_ state-atom {old-type :type :as _old-state} {new-type :type :as _new-state}]
  (when (not= old-type new-type)
    (swap! state-atom assoc
      :qr-data
      (if (= new-type :profile)
        profile-link
        wallet-address))))

(defn view
  []
  (let [state (reagent/atom {:type                :profile
                             :address             :legacy
                             :qr-data             profile-link
                             :on-share-press      #(js/alert "share pressed")
                             :on-text-press       #(js/alert "text pressed")
                             :on-text-long-press  #(js/alert "text long press")
                             :profile-picture     (resources/get-mock-image :user-picture-female2)
                             :full-name           "My User"
                             :customization-color :purple
                             :wallet-user-avatar  "Alicia Keys"
                             :emoji               "🐈"
                             :on-info-press       #(js/alert "Info pressed")
                             :on-legacy-press     #(js/alert (str "Tab " % " pressed"))
                             :on-multichain-press #(js/alert (str "Tab " % " pressed"))
                             :networks            (take 2 possible-networks)
                             :on-settings-press   #(js/alert "Settings pressed")})
        _ (add-watch state :change set-qr-data-based-on-type)]
    (fn []
      (let [qr-url              (if (= (:type @state) :wallet-multichain)
                                  (as-> (:networks @state) $
                                    (map get-network-short-name-url $)
                                    (apply str $)
                                    (str $ (:qr-data @state)))
                                  (:qr-data @state))
            qr-media-server-uri (image-server/get-qr-image-uri-for-any-url
                                 {:url         qr-url
                                  :port        (rf/sub [:mediaserver/port])
                                  :qr-size     500
                                  :error-level :highest})
            typed-descriptor    (concat descriptor
                                        (case (:type @state)
                                          :profile           profile-descriptor
                                          :wallet wallet-descriptor
                                          :saved-address saved-address-descriptor
                                          :watched-address watched-address-descriptor
                                          nil))]
        [preview/preview-container
         {:state                     state
          :descriptor                typed-descriptor
          :component-container-style {:padding-horizontal 0}}
         [rn/view
          {:style {:flex               1
                   :justify-content    :flex-end
                   :align-items        :center
                   :padding-horizontal 20
                   :padding-vertical   30}}
          [rn/view
           {:style {:position :absolute
                    :top      0
                    :bottom   0
                    :left     0
                    :right    0}}
           [rn/image
            {:style  {:flex        1
                      :resize-mode :stretch}
             :source (resources/get-mock-image :dark-blur-bg)}]]
          [quo/share-qr-code
           (assoc @state
                  :qr-image-uri qr-media-server-uri
                  :qr-data      qr-url)]]]))))
