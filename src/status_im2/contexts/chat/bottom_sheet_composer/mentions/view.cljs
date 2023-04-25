(ns status-im2.contexts.chat.bottom-sheet-composer.mentions.view
  (:require
    [reagent.core :as reagent]
    [status-im2.contexts.chat.bottom-sheet-composer.utils :as utils]
    [utils.re-frame :as rf]
    [react-native.core :as rn]
    [react-native.reanimated :as reanimated]
    [status-im2.common.contact-list-item.view :as contact-list-item]
    [status-im2.contexts.chat.bottom-sheet-composer.mentions.style :as style]))

(defn mention-item
  [user]
  [contact-list-item/contact-list-item
   {:on-press #(rf/dispatch [:chat.ui/select-mention nil user])} user])

(defn- f-view
  [suggestions suggestions-atom state animations max-height cursor-pos]
  (let [opacity (reanimated/use-shared-value (if (seq suggestions) 1 0))
        mentions-pos
        (utils/calc-suggestions-position cursor-pos state animations max-height (count suggestions))]
    (rn/use-effect
     (fn []
       (if (seq suggestions)
         (reagent/next-tick #(reset! suggestions-atom suggestions))
         (js/setTimeout #(reset! suggestions-atom suggestions) 300))
       (reanimated/animate opacity (if (seq suggestions) 1 0)))
     [(seq suggestions)])
    [reanimated/view
     {:style (style/container opacity mentions-pos)}
     [rn/flat-list
      {:keyboard-should-persist-taps :always
       :data                         (vals @suggestions-atom)
       :key-fn                       :key
       :render-fn                    mention-item
       :accessibility-label          :mentions-list}]]))

(defn view
  [state animations max-height cursor-pos]
  (let [suggestions      (rf/sub [:chat/mention-suggestions])
        suggestions-atom (reagent/atom {})]
    [:f> f-view suggestions suggestions-atom state animations max-height cursor-pos]))
