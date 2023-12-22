(ns status-im.contexts.preview-screens.quo-preview.share.qr-code
  (:require
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im.common.resources :as resources]
    [status-im.contexts.preview-screens.quo-preview.preview :as preview]
    [utils.image-server :as image-server]
    [utils.re-frame :as rf]))

(def descriptor
  [{:key  :url
    :type :text}
   {:key     :avatar
    :type    :select
    :options [{:key :none}
              {:key :profile}
              {:key :wallet-account}
              {:key :community}
              {:key :channel}
              {:key :saved-address}]}
   {:key     :size
    :type    :select
    :options [{:key 250} {:key 311} {:key 350}]}])

(def profile-descriptor
  [{:key     :profile-picture
    :type    :select
    :options [{:key   (resources/get-mock-image :user-picture-male5)
               :value "User"}
              {:key   nil
               :value "None"}]}
   (preview/customization-color-option)
   {:key  :full-name
    :type :text}])

(def wallet-account-descriptor
  [{:key     :emoji
    :type    :select
    :options [{:key "🍒"}
              {:key "🐧"}
              {:key "🍨"}]}
   (preview/customization-color-option)])

(def channel-descriptor
  [{:key     :emoji
    :type    :select
    :options [{:key "🍒"}
              {:key "🐧"}
              {:key "🍨"}]}
   (preview/customization-color-option)])

(def saved-address-descriptor
  [{:key  :f-name
    :type :text}
   {:key  :l-name
    :type :text}
   (preview/customization-color-option)])

(defn view
  []
  (let [media-server-port (rf/sub [:mediaserver/port])
        state             (reagent/atom
                           {:url "https://status.app/u#zQ34e1zlOdas0pKnvrweeedsasas12adjie8"
                            :size 250
                            :avatar :none
                            :profile-picture (resources/get-mock-image :user-picture-male5)
                            :customization-color :army
                            :full-name "Full Name"
                            :emoji "🍒"
                            :picture (resources/get-mock-image :community-logo)
                            :f-name "First Name"
                            :l-name "Last Name"})]
    (fn []
      (let [qr-media-server-uri (image-server/get-qr-image-uri-for-any-url
                                 {:url         (:url @state)
                                  :qr-size     (:size @state)
                                  :port        media-server-port
                                  :error-level :highest})]
        [preview/preview-container
         {:component-container-style {:flex            1
                                      :justify-content :center
                                      :align-items     :center
                                      :margin-vertical 12}
          :state                     state
          :descriptor                (concat descriptor
                                             (case (:avatar @state)
                                               :profile        profile-descriptor
                                               :wallet-account wallet-account-descriptor
                                               :channel        channel-descriptor
                                               :saved-address  saved-address-descriptor
                                               nil))}
         [quo/qr-code
          (cond-> @state
            :always
            (assoc :qr-image-uri qr-media-server-uri)

            ;; `:channel` variant receives colors as hex strings instead of keywords
            (= (:avatar @state) :channel)
            (update :customization-color colors/custom-color 60))]

         [rn/view {:style {:margin 12}}
          [quo/text "URL:"]
          [quo/text (:url @state)]]]))))
