(ns status-im2.contexts.quo-preview.share.qr-code
  (:require [quo.components.text :as text]
            [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.common.resources :as resources]
            [status-im2.contexts.quo-preview.preview :as preview]
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

(defn preview
  []
  (let [state (reagent/atom
               {:url                 "https://join.status.im/status"
                :media-server-port   (rf/sub [:mediaserver/port])
                :size                250
                :avatar              :none
                :profile-picture     (resources/get-mock-image :user-picture-male5)
                :customization-color :army
                :full-name           "Full Name"
                :emoji               "🍒"
                :picture             (resources/get-mock-image :community-logo)
                :f-name              "First Name"
                :l-name              "Last Name"})]
    (fn []
      [preview/preview-container
       {:state      state
        :descriptor (concat descriptor
                            (case (:avatar @state)
                              :profile        profile-descriptor
                              :wallet-account wallet-account-descriptor
                              :channel        channel-descriptor
                              :saved-address  saved-address-descriptor
                              nil))}
       [rn/view
        {:style {:flex            1
                 :justify-content :center
                 :align-items     :center
                 :margin-vertical 12}}
        [quo/qr-code
         (cond-> @state
           ;; `:channel` variant receives colors as hex strings instead of keywords
           (= (:avatar @state) :channel)
           (update :customization-color colors/custom-color 60))]

        [rn/view {:style {:margin 12}}
         [text/text "URL:"]
         [text/text (:url @state)]]]])))
