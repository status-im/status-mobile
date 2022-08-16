(ns status-im.switcher.bottom-tabs
  (:require [quo.react-native :as rn]
            [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [quo2.reanimated :as reanimated]
            [status-im.switcher.styles :as styles]
            [status-im.switcher.constants :as constants]
            [status-im.switcher.animation :as animation]
            [status-im.ui.components.icons.icons :as icons]))

(def selected-stack-id (atom :communities-stack))

;; Reagent atoms used for lazily loading home screen tabs
(def load-communities-tab? (reagent/atom true))
(def load-chats-tab? (reagent/atom false))
(def load-wallet-tab? (reagent/atom false))
(def load-browser-tab? (reagent/atom false))

(re-frame/reg-fx
 :new-ui/reset-bottom-tabs
 (fn []
   (reset! selected-stack-id :communities-stack)
   (reset! load-communities-tab? true)
   (reset! load-chats-tab? false)
   (reset! load-wallet-tab? false)
   (reset! load-browser-tab? false)))

(defn bottom-tab-on-press [shared-values stack-id]
  (when-not (= stack-id @selected-stack-id)
    (reset! selected-stack-id stack-id)
    (animation/bottom-tab-on-press shared-values stack-id)
    (case stack-id
      :communities-stack (reset! load-communities-tab? true)
      :chats-stack       (reset! load-chats-tab? true)
      :wallet-stack      (reset! load-wallet-tab? true)
      :browser-stack     (reset! load-browser-tab? true))))

;; TODO(parvesh) - reimplement tab with counter, once design is complete
(defn bottom-tab [icon stack-id icons-only? shared-values]
  [:f>
   (fn []
     (let [bottom-tab-original-style {:padding 16}]
       (if icons-only?
         [rn/touchable-opacity {:active-opacity 1
                                :style          bottom-tab-original-style
                                :on-press       #(bottom-tab-on-press shared-values stack-id)}
          [reanimated/view {:style (reanimated/apply-animations-to-style
                                    {:opacity (get
                                               shared-values
                                               (get constants/tabs-opacity-keywords stack-id))}
                                    {})}
           [icons/icon icon (styles/bottom-tab-icon :bottom-tabs-selected-tab)]]]
         [rn/view {:style bottom-tab-original-style}
          [icons/icon icon (styles/bottom-tab-icon :bottom-tabs-non-selected-tab)]])))])

(defn tabs [shared-values icons-only?]
  [rn/view {:style (styles/bottom-tabs icons-only?)}
   [bottom-tab :main-icons/redesign-communities24 :communities-stack icons-only? shared-values]
   [bottom-tab :main-icons/redesign-messages24 :chats-stack icons-only? shared-values]
   [rn/view {:width 50}]
   [bottom-tab :main-icons/redesign-wallet24 :wallet-stack icons-only? shared-values]
   [bottom-tab :main-icons/redesign-browser24 :browser-stack icons-only? shared-values]])

(defn bottom-tabs [shared-values]
  [:<>
   [tabs shared-values false]
   [tabs shared-values true]])
