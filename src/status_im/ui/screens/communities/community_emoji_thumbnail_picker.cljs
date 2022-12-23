(ns status-im.ui.screens.communities.community-emoji-thumbnail-picker
  (:require [clojure.string :as string]
            [quo.core :as quo]
            [quo.react-native :as rn]
            [status-im.communities.core :as communities]
            [status-im.ui.components.emoji-thumbnail.color-picker :as color-picker]
            [status-im.ui.components.emoji-thumbnail.preview :as emoji-thumbnail-preview]
            [status-im.ui.components.emoji-thumbnail.styles :as styles]
            [status-im.ui.components.keyboard-avoid-presentation :as kb-presentation]
            [status-im.ui.components.react :as react]
            [utils.re-frame :as rf]))

(defn thumbnail-preview-section
  []
  (let [{:keys [color emoji]} (rf/sub [:communities/create-channel])
        size                  styles/emoji-thumbnail-preview-size]
    [rn/view styles/emoji-thumbnail-preview
     [emoji-thumbnail-preview/emoji-thumbnail
      emoji color size]]))

(defn color-circle
  [item]
  (let [{:keys [color]} (rf/sub [:communities/create-channel])
        item-color      (:color item)
        key             (:key key)
        color-selected? (= (string/lower-case item-color) (string/lower-case color))]
    [react/touchable-opacity
     {:key                 key
      :accessibility-label :color-circle
      :on-press            #(rf/dispatch [::communities/create-channel-field :color item-color])}
     [rn/view {:style (styles/emoji-picker-color-border item-color color-selected?)}
      [rn/view {:style (styles/emoji-picker-color item-color)}]]]))

(defn update-emoji
  [emoji]
  (when-not (string/blank? emoji)
    (rf/dispatch [::communities/create-channel-field :emoji emoji])))

(defn emoji-keyboard-section
  []
  (let [{:keys [width height]} (rf/sub [:dimensions/window])
        keyboard_height        (if (> width height)
                                 400
                                 (- height styles/emoji-picker-upper-components-size))]
    [rn/view {:style {:height keyboard_height} :accessibility-label :emoji-keyboard-container}
     [rn/emoji-keyboard (styles/emoji-keyboard #(update-emoji (.-emoji ^js %)))]]))

(defn view
  []
  [kb-presentation/keyboard-avoiding-view {:style {:flex 1}}
   [rn/scroll-view
    [quo/separator]
    [thumbnail-preview-section]
    [color-picker/color-picker-section color-circle]
    [emoji-keyboard-section]]])
