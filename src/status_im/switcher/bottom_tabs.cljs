(ns status-im.switcher.bottom-tabs
  (:require [quo.react-native :as rn]
            [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [status-im.switcher.styles :as styles]
            [status-im.switcher.constants :as constants]
            [status-im.switcher.animation :as animation]
            [quo2.components.navigation.bottom-nav-tab :as bottom-nav-tab]))

;; Reagent atoms used for lazily loading home screen tabs
(def load-communities-tab? (reagent/atom false))
(def load-chats-tab? (reagent/atom false))
(def load-wallet-tab? (reagent/atom false))
(def load-browser-tab? (reagent/atom false))

(defn load-selected-stack [stack-id]
  (case stack-id
    :communities-stack (reset! load-communities-tab? true)
    :chats-stack       (reset! load-chats-tab? true)
    :wallet-stack      (reset! load-wallet-tab? true)
    :browser-stack     (reset! load-browser-tab? true)
    ""))

(re-frame/reg-fx
 :new-ui/reset-bottom-tabs
 (fn []
   (let [selected-stack-id @animation/selected-stack-id]
     (reset! load-communities-tab? (= selected-stack-id :communities-stack))
     (reset! load-chats-tab? (= selected-stack-id :chats-stack))
     (reset! load-wallet-tab? (= selected-stack-id :wallet-stack))
     (reset! load-browser-tab? (= selected-stack-id :browser-stack)))))

(defn bottom-tab-on-press [shared-values stack-id]
  (when-not (= stack-id @animation/selected-stack-id)
    (let [stack-load-delay (if @animation/home-stack-open? 0 constants/shell-animation-time)]
      (animation/bottom-tab-on-press shared-values stack-id)
      (js/setTimeout #(load-selected-stack stack-id) stack-load-delay))))

(defn bottom-tab [icon stack-id shared-values]
  [bottom-nav-tab/bottom-nav-tab
   {:icon                icon
    :icon-color-anim     (get
                          shared-values
                          (get constants/tabs-icon-color-keywords stack-id))
    :on-press            #(bottom-tab-on-press shared-values stack-id)
    :accessibility-label (str (name stack-id) "-tab")}])

(defn bottom-tabs [shared-values]
  (load-selected-stack @animation/selected-stack-id)
  [rn/view {:style (styles/bottom-tabs-container false)}
   [rn/view {:style (styles/bottom-tabs)}
    [bottom-tab :i/communities :communities-stack shared-values]
    [bottom-tab :i/messages :chats-stack shared-values]
    [bottom-tab :i/wallet :wallet-stack shared-values]
    [bottom-tab :i/browser :browser-stack  shared-values]]])
