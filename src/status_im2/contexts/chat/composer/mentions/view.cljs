(ns status-im2.contexts.chat.composer.mentions.view
  (:require
    [react-native.safe-area :as safe-area]
    [reagent.core :as reagent]
    [status-im2.contexts.chat.composer.utils :as utils]
    [utils.re-frame :as rf]
    [react-native.core :as rn]
    [react-native.reanimated :as reanimated]
    [status-im2.common.contact-list-item.view :as contact-list-item]
    [status-im2.contexts.chat.composer.mentions.style :as style]))


(defn mention-item
  [user _ _ _]
  [contact-list-item/contact-list-item
   {:on-press (fn []
                (rf/dispatch [:chat.ui/select-mention nil user]))}
   user])

(defn- f-view
  [suggestions-atom props state animations max-height cursor-pos images link-previews? reply edit]
  (let [suggestions  (rf/sub [:chat/mention-suggestions])
        opacity      (reanimated/use-shared-value (if (seq suggestions) 1 0))
        size         (count suggestions)
        data         {:keyboard-height @(:kb-height state)
                      :insets          (safe-area/get-insets)
                      :curr-height     (reanimated/get-shared-value (:height animations))
                      :window-height   (:height (rn/get-window))
                      :images          images
                      :link-previews?  link-previews?
                      :reply           reply
                      :edit            edit}
        mentions-pos (utils/calc-suggestions-position cursor-pos max-height size state data)]
    (rn/use-effect
     (fn []
       (if (seq suggestions)
         (reset! suggestions-atom suggestions)
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
  [props state animations max-height cursor-pos images link-previews? reply edit]
  (let [suggestions-atom (reagent/atom {})]
    [:f> f-view suggestions-atom props state animations max-height cursor-pos images link-previews? reply
     edit]))
