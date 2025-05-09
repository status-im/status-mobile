(ns status-chat.onboarding.view
  (:require [react-native.core :as rn]
            [status-chat.components.chat.body :as chat.body]
            [status-chat.components.chat.composer :as chat.composer]
            [status-chat.components.chat.container :as chat.container]
            [status-chat.components.chat.header :as chat.header]
            [status-chat.components.chat.message :as chat.message]
            [status-chat.onboarding.steps.core :as steps]
            [utils.re-frame :as rf]))

(defn- render-step
  [step]
  (let [reply             (rf/sub [:onboarding/reply-for-step step])
        color             (rf/sub [:onboarding/color])
        message-component (get-in steps/steps [step :message])]
    [chat.message/container
     {:customization-color color}
     [message-component
      {:reply reply}]]))

(defn- composer
  []
  (let [current-step    (rf/sub [:onboarding/current-step])
        inner-component (get-in steps/steps [current-step :composer])]
    [inner-component]))

(defn view
  []
  (let [steps-list (rf/sub [:onboarding/steps-list])
        color      (rf/sub [:onboarding/color])]
    [chat.container/view
     #_[chat.header/view]
     [chat.body/view
      [rn/flat-list
       {:data      steps-list
        :render-fn render-step
        :key-fn    (fn [step idx] (str step "-" idx))
        :inverted  true}]]
     [chat.composer/container {:color color}
      [composer]]]))
