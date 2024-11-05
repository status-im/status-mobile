(ns status-im.contexts.chat.messenger.composer.mentions.view
  (:require
    [quo.theme]
    [react-native.core :as rn]
    [react-native.reanimated :as reanimated]
    [react-native.safe-area :as safe-area]
    [status-im.common.contact-list-item.view :as contact-list-item]
    [status-im.contexts.chat.messenger.composer.constants :as constants]
    [status-im.contexts.chat.messenger.composer.mentions.style :as style]
    [status-im.contexts.chat.messenger.messages.constants :as messages.constants]
    [utils.re-frame :as rf]))

(defn mention-item
  [user]
  [contact-list-item/contact-list-item
   {:on-press #(rf/dispatch [:chat.ui/select-mention user])}
   user])

(defn view
  [layout-height]
  (let [suggestions                               (rf/sub [:chat/mention-suggestions])
        suggestions?                              (seq suggestions)
        theme                                     (quo.theme/use-theme)
        opacity                                   (reanimated/use-shared-value (if suggestions? 1 0))
        [suggestions-state set-suggestions-state] (rn/use-state suggestions)
        top                                       (min constants/mentions-max-height
                                                       (* (count suggestions-state) 56)
                                                       (- @layout-height
                                                          (+ (safe-area/get-top)
                                                             messages.constants/top-bar-height
                                                             5)))]
    (rn/use-effect
     (fn []
       (if suggestions?
         (set-suggestions-state suggestions)
         (js/setTimeout #(set-suggestions-state suggestions) 300))
       (reanimated/animate opacity (if suggestions? 1 0)))
     [suggestions?])
    [reanimated/view
     {:style (style/container opacity top theme)}
     [rn/flat-list
      {:keyboard-should-persist-taps :always
       :data                         (vals suggestions-state)
       :key-fn                       :key
       :render-fn                    mention-item
       :accessibility-label          :mentions-list}]]))
