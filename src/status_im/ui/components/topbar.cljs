(ns status-im.ui.components.topbar
  (:require [re-frame.core :as re-frame]
            [quo.core :as quo]
            [quo2.foundations.colors :as quo2.colors]
            [quo2.components.header :as quo2.header]
            [quo2.components.icon :as quo2.icon]
            [status-im.ui.components.react :as react]))

(def default-button-width 48)

(defn default-navigation [modal? {:keys [on-press sheet? label icon]
                                  :or   {sheet?           false}}
                          close-icon-props
                          close-icon-container-props]
  (cond-> {:icon                [react/view close-icon-container-props
                                 [quo2.icon/icon (if modal?
                                                   :i/close
                                                   :i/arrow-left) close-icon-props]]
           :accessibility-label :back-button
           :on-press            #(re-frame/dispatch [(if sheet?
                                                       :bottom-sheet/hide
                                                       :navigate-back)])}
    on-press
    (assoc :on-press on-press)

    icon
    (assoc :icon icon)

    label
    (dissoc :icon)

    label
    (assoc :label label)))

(defn topbar [{:keys [navigation use-insets right-accessories modal? content border-bottom? new-ui? close-icon-props header-style close-icon-container-props] ;; remove new-ui? key, temp fix
               :or   {border-bottom? true
                      new-ui?        false}
               :as   props}]
  (let [navigation (if (= navigation :none)
                     nil
                     [(default-navigation modal? navigation close-icon-props close-icon-container-props)])]
    [quo/safe-area-consumer
     (fn [insets]
       [quo2.header/header (merge {:left-accessories navigation
                                   :title-component  content
                                   :header-style     header-style
                                   :insets           (when use-insets insets)
                                   :left-width       (when navigation
                                                       default-button-width)
                                   :border-bottom    border-bottom?}
                                  props
                                  (when (seq right-accessories)
                                    {:right-accessories right-accessories})
                                  (when new-ui?
                                    {:background (quo2.colors/theme-colors quo2.colors/neutral-5 quo2.colors/neutral-95)}))])]))
