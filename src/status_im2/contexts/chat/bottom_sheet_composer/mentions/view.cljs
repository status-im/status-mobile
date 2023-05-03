(ns status-im2.contexts.chat.bottom-sheet-composer.mentions.view
  (:require
    [react-native.platform :as platform]
    [reagent.core :as reagent]
    [status-im2.contexts.chat.bottom-sheet-composer.utils :as utils]
    [utils.re-frame :as rf]
    [react-native.core :as rn]
    [react-native.reanimated :as reanimated]
    [status-im2.common.contact-list-item.view :as contact-list-item]
    [status-im2.contexts.chat.bottom-sheet-composer.mentions.style :as style]))

(defn mention-item
  [user _ _ {:keys [cursor-position input-ref]}]
  [contact-list-item/contact-list-item
   {:on-press (fn []
                (let [new-cursor-pos (+ (count (:primary-name user)) @cursor-position)]
                  (rf/dispatch [:chat.ui/select-mention nil user])
                  (when platform/android?
                    (reset! cursor-position new-cursor-pos)
                    (reagent/next-tick #(when @input-ref
                                          (.setNativeProps ^js @input-ref
                                                           (clj->js {:selection {:start new-cursor-pos
                                                                                 :end
                                                                                 new-cursor-pos}})))))))}
   user])

(defn- f-view
  [suggestions suggestions-atom props state animations max-height cursor-pos]
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
       :render-data                  {:cursor-position (:cursor-position state)
                                      :input-ref       (:input-ref props)}
       :accessibility-label          :mentions-list}]]))

(defn view
  [props state animations max-height cursor-pos]
  (let [suggestions      (rf/sub [:chat/mention-suggestions])
        suggestions-atom (reagent/atom {})]
    [:f> f-view suggestions suggestions-atom props state animations max-height cursor-pos]))
