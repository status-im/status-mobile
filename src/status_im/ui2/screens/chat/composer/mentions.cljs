(ns status-im.ui2.screens.chat.composer.mentions
  (:require [quo2.reanimated :as reanimated]
            [status-im.ui.components.list.views :as list]
            [quo.components.list.item :as list-item]
            [status-im.ui.screens.chat.photos :as photos]
            [quo.components.text :as text]
            [quo.react-native :as rn]
            [quo.react]
            [status-im.utils.handlers :refer [>evt]]))

(defn mention-item
  [[public-key {:keys [alias name nickname] :as user}] _ _ text-input-ref]
  (let [ens-name? (not= alias name)]
    [rn/touchable-opacity {:on-press #(>evt [:chat.ui/select-mention text-input-ref user]) :style {:border-width 1 :border-color :red}}
     ;;TODO quo2 item should be used
     [list-item/list-item
      (cond-> {:icon              [photos/member-photo public-key]
               :size              :small
               :text-size         :small
               :title
               [text/text
                {:weight          :medium
                 :ellipsize-mode  :tail
                 :number-of-lines 1
                 :size            :small}
                (if nickname
                  nickname
                  name)
                (when nickname
                  [text/text
                   {:weight         :regular
                    :color          :secondary
                    :ellipsize-mode :tail
                    :size           :small}
                   " "
                   (when ens-name?
                     "@")
                   name])]
               :title-text-weight :medium}
        ens-name?
        (assoc :subtitle alias))]]))

(defn autocomplete-mentions [suggestions]
  [:f>
   (fn []
     (let [animation (reanimated/use-shared-value 0)]
       (quo.react/effect! #(reanimated/set-shared-value animation (reanimated/with-timing (if (seq suggestions) 0 200))))
       [reanimated/view {:style (reanimated/apply-animations-to-style
                                 {:transform [{:translateY animation}]}
                                 {:bottom 0 :position :absolute :z-index 5 :max-height 180})}
        [list/flat-list
         {:keyboardShouldPersistTaps :always
          :data                      suggestions
          :key-fn                    first
          :render-fn                 mention-item
          :content-container-style   {:padding-bottom 12}}]]))])