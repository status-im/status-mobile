(ns status-im.ui.screens.communities.create-channel
  (:require [clojure.string :as string]
            [quo.core :as quo]
            [quo.react-native :as rn]
            [status-im.communities.core :as communities]
            [utils.i18n :as i18n]
            [status-im.ui.components.emoji-thumbnail.preview :as emoji-thumbnail-preview]
            [status-im.ui.components.emoji-thumbnail.styles :as styles]
            [status-im.ui.components.keyboard-avoid-presentation :as kb-presentation]
            [status-im.ui.components.toolbar :as toolbar]
            [status-im.ui.screens.communities.create :as create]
            [utils.re-frame :as rf]
            [utils.debounce :as debounce]))

(defn valid?
  [channel-name channel-description]
  (and (not (string/blank? channel-name))
       (not (string/blank? channel-description))
       (<= (count channel-name) create/max-name-length)
       (<= (count channel-description) create/max-description-length)))

(defn thumbnail
  []
  (let [{:keys [color emoji]} (rf/sub [:communities/create-channel])
        size                  styles/emoji-thumbnail-preview-size]
    [rn/view styles/emoji-thumbnail-preview
     [emoji-thumbnail-preview/emoji-thumbnail-touchable
      emoji color size
      #(rf/dispatch [:open-modal :community-emoji-thumbnail-picker nil])]]))

(defn form
  []
  (let [{:keys [name description]} (rf/sub [:communities/create-channel])]
    [rn/scroll-view
     {:style                   {:flex 1}
      :content-container-style {:padding-bottom 16}}
     [:<>
      [thumbnail]
      [rn/view {:padding-horizontal 16}
       [quo/text-input
        {:placeholder    (i18n/label :t/name-your-channel-placeholder)
         :on-change-text #(rf/dispatch [::communities/create-channel-field :name %])
         :default-value  name
         :auto-focus     false}]]
      [quo/separator {:style {:margin-vertical 16}}]
      [rn/view {:padding-horizontal 16}
       [create/countable-label
        {:label      (i18n/label :t/description)
         :text       description
         :max-length create/max-description-length}]
       [quo/text-input
        {:placeholder    (i18n/label :t/give-a-short-description-community)
         :multiline      true
         :default-value  description
         :on-change-text #(rf/dispatch [::communities/create-channel-field :description %])}]]]]))

(defn view
  []
  (let [{:keys [name description]} (rf/sub [:communities/create-channel])]
    [kb-presentation/keyboard-avoiding-view {:style {:flex 1}}
     [quo/separator]
     [form]
     [toolbar/toolbar
      {:show-border? true
       :center
       [quo/button
        {:disabled (not (valid? name description))
         :type     :secondary
         :on-press #(debounce/dispatch-and-chill
                     [::communities/create-channel-confirmation-pressed]
                     3000)}
        (i18n/label :t/create)]}]]))
