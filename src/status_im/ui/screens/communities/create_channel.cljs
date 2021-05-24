(ns status-im.ui.screens.communities.create-channel
  (:require [clojure.string :as str]
            [quo.react-native :as rn]
            [quo.core :as quo]
            [status-im.i18n.i18n :as i18n]
            [status-im.ui.components.toolbar :as toolbar]
            [status-im.communities.core :as communities]
            [status-im.utils.debounce :as debounce]
            [status-im.utils.handlers :refer [>evt <sub]]
            [status-im.ui.screens.communities.create :as create]))

(defn valid? [channel-name channel-description]
  (and (not (str/blank? channel-name))
       (not (str/blank? channel-description))
       (<= (count channel-name) create/max-name-length)
       (<= (count channel-description) create/max-description-length)))

(defn form []
  (let [{:keys [name description]} (<sub [:communities/create-channel])]
    [rn/scroll-view {:style                   {:flex 1}
                     :content-container-style {:padding-vertical 16}}
     [rn/view {:style {:padding-bottom     16
                       :padding-top        10
                       :padding-horizontal 16}}
      [rn/view
       [create/countable-label {:label      (i18n/label :t/name)
                                :text       name
                                :max-length create/max-name-length}]
       [quo/text-input
        {:placeholder    (i18n/label :t/name-your-channel-placeholder)
         :on-change-text #(>evt  [::communities/create-channel-field :name %])
         :default-value  name
         :auto-focus     true}]]
      [quo/separator {:style {:margin-vertical 10}}]
      [rn/view
       [create/countable-label {:label      (i18n/label :t/description)
                                :text      description
                                :max-length create/max-description-length}]
       [quo/text-input
        {:placeholder    (i18n/label :t/give-a-short-description-community)
         :multiline      true
         :default-value  description
         :on-change-text #(>evt  [::communities/create-channel-field :description %])}]]]]))

(defn view []
  (let [{:keys [name description]} (<sub [:communities/create-channel])]
    [:<>
     [form]
     [toolbar/toolbar
      {:show-border? true
       :center
       [quo/button {:disabled (not (valid? name description))
                    :type     :secondary
                    :on-press #(debounce/dispatch-and-chill
                                [::communities/create-channel-confirmation-pressed]
                                3000)}
        (i18n/label :t/create)]}]]))
