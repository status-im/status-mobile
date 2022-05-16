(ns status-im.ui.components.topbar-redesign
  (:require
   [re-frame.core :as re-frame]
   [quo.components.header-redesign :as header-redesign]
   [quo.core :as quo]))

(def default-button-width 48)

(defn default-navigation [modal? {:keys [on-press label icon]}]
  (cond-> {:icon                (if modal? :main-icons/close :main-icons/arrow-left)
           :accessibility-label :back-button
           :on-press            #(re-frame/dispatch [:navigate-back])}
    on-press
    (assoc :on-press on-press)
    icon
    (assoc :icon icon)
    label
    (dissoc :icon)
    label
    (assoc :label label)))

(defn topbar [{:keys [navigation use-insets left-accessories right-accessories modal? content]
               :as   props}]
  (let [navigation (if (= navigation :none)
                     nil
                     [(default-navigation modal? navigation)])]
    [quo/safe-area-consumer
     (fn [insets]
       [header-redesign/header (merge {:left-accessories (if (= navigation nil?)
                                                           left-accessories
                                                           navigation)
                                       :title-component  content
                                       :insets           (when use-insets insets)
                                       :left-width       (when navigation
                                                           default-button-width)}
                                      props
                                      (when (seq right-accessories)
                                        {:right-accessories right-accessories}))])]))