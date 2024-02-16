(ns legacy.status-im.ui.components.topbar
  (:require
    [legacy.status-im.ui.components.core :as quo]
    [quo.foundations.colors :as quo.colors]
    [re-frame.core :as re-frame]
    [react-native.safe-area :as safe-area]))

(def default-button-width 48)

(defn default-navigation
  [modal? {:keys [on-press label icon]}]
  (cond-> {:icon                (if modal? :i/close :i/arrow-left)
           :accessibility-label :back-button
           :on-press            #(re-frame/dispatch [:navigate-back :bottom-sheet/hide-old])}
    on-press
    (assoc :on-press on-press)

    icon
    (assoc :icon icon)

    label
    (dissoc :icon)

    label
    (assoc :label label)))

(defn topbar
  [{:keys [navigation use-insets right-accessories modal? content border-bottom? new-ui?] ;; remove
                                                                                          ;; new-ui? key,
                                                                                          ;; temp fix
    :or   {border-bottom? true
           new-ui?        false}
    :as   props}]
  (let [navigation (if (= navigation :none)
                     nil
                     [(default-navigation modal? navigation)])]
    [quo/header
     (merge {:left-accessories navigation
             :title-component  content
             :insets           (when use-insets (safe-area/get-insets))
             :left-width       (when navigation
                                 default-button-width)
             :border-bottom    border-bottom?}
            props
            (when (seq right-accessories)
              {:right-accessories right-accessories})
            (when new-ui?
              {:background (quo.colors/theme-colors quo.colors/neutral-5
                                                    quo.colors/neutral-95)}))]))
