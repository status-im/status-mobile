(ns status-im.switcher.switcher-container
  (:require [quo.react-native :as rn]
            status-im.switcher.cards.messaging-card
            [status-im.switcher.styles :as styles]
            [status-im.utils.handlers :refer [<sub]]
            [status-im.switcher.cards.messaging-card :as messaging-card]))

;; TODO - use something like this to avoid multiple renders etc.
;; (defn switch-screen [toggle-switcher-screen]
;;   (let [cards (<sub [:navigation2/switcher-cards])
;;         new-cards (reduce (fn [acc card]
;;                             (conj acc (assoc card :toggle-switcher-screen toggle-switcher-screen)))
;;                           () cards)]
;;     (fn []
;;       [rn/view {:style (styles/switcher-switch-screen)}
;;        [rn/flat-list {:width                     352
;;                       :data                      new-cards
;;                       :render-fn                 messaging-card/card
;;                       :num-columns               2
;;                       :key-fn                    str}]])))

(defn switch-screen [toggle-switcher-screen]
  (let [cards (<sub [:navigation2/switcher-cards toggle-switcher-screen])]
    [rn/view {:style (styles/switcher-switch-screen)}
     [rn/flat-list {:width                     352
                    :data                      cards
                    :render-fn                 messaging-card/card
                    :num-columns               2
                    :key-fn                    str}]]))

(defn tabs [toggle-switcher-screen]
  [switch-screen toggle-switcher-screen])
