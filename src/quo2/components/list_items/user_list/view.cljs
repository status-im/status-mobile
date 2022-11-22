(ns quo2.components.list-items.user-list.view
  (:require [quo2.components.avatars.user-avatar :as user-avatar]
            [quo2.components.icon :as icons]
            [quo2.components.list-items.user-list.style :as style]
            [quo2.components.markdown.text :as text]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [quo2.components.selectors.selectors :as selectors]
            [status-im.multiaccounts.core :as multiaccounts]
            [status-im.utils.utils :as utils]))

(def ^:private icon-types {:options :i/options
                           :close   :i/close})

(defn- cant-be-invited-msg []
  [rn/view {:style               style/cant-be-invited
            :accessibility-label "user can't be invite"}
   [icons/icon :i/info {:size            12
                        :color           colors/neutral-40
                        :container-style style/icon-container}]
   [text/text {:weight :regular
               :size   :label
               :style  style/cant-be-invited-text-style}
    "Can’t be invited due to their privacy settings"]])

(defn- right-section [{:keys [id button-type on-press checked?]}]
  (if (= button-type :checkbox)
    [selectors/checkbox {:default-checked? checked?}]
    [rn/touchable-opacity
     {:on-press            on-press
      :accessibility-label (str (or id "user-list") "-" (if (= button-type :options)
                                                          "options"
                                                          "close"))}
     [rn/view {:style (when (= button-type :close)
                        style/icon-container-styles)}
      [icons/icon (button-type icon-types) {:size            20
                                            :color           (colors/theme-colors colors/neutral-100 colors/white)}]]]))

(defn- user-info [{:keys [id contact on-press checked? button-type]}]
  [rn/view {:style style/container}
   [rn/view style/row-centered
    [user-avatar/user-avatar {:size              :small
                              :ring?             true
                              :online?           true
                              :profile-picture   (multiaccounts/displayed-photo contact)
                              :status-indicator? true}]
    [rn/view {:style style/ml-8}
     [text/text {:weight :semi-bold
                 :size   :paragraph-1
                 :style  {:color (colors/theme-colors colors/neutral-100 colors/white)}} (-> contact
                                                                                             (multiaccounts/contact-two-names false))]
     [text/text {:weight :regular
                 :size   :paragraph-2
                 :style  {:color (colors/theme-colors colors/neutral-50 colors/neutral-40)}} (utils/get-shortened-address (:public-key contact))]]]
   [right-section {:id          id
                   :button-type button-type
                   :on-press    on-press
                   :checked?    checked?}]])

(defn user-list [{:keys [id contact on-press background-type checked? button-type can-be-invited?]}]
  [rn/view {:style (style/user-list-wrapper can-be-invited? background-type)}
   [user-info {:id          id
               :on-press    on-press
               :checked?    checked?
               :button-type button-type
               :contact     contact}]
   (when-not can-be-invited? [cant-be-invited-msg])])
