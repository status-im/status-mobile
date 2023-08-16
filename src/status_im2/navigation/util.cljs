(ns status-im2.navigation.util
  (:require [react-native.navigation :as navigation]
            [reagent.core :as reagent]))

(defn create-class-and-bind
  "Creates a React class that allows the use of life-cycle methods added by
  react-native-navigation:
   - componentWillAppear
   - componentDidAppear
   - componentDidDisappear
  Receives:
  - `component-id` - The component-id to subscribe registered in navigation
  - `react-methods` - A map of React methods (kebab-case) -> function handler
  - `reagent-render` - A regular reagent function that returns hiccup.
  Example:
  (defn view
    [props & children]
    ;; Bindings executed when component is created
    (let [qr-code-succeed? (reagent/atom false)]
      (create-class-and-bind
       \"sign-in-intro\" ; navigation component-id of the screen to subscribe
       {:component-did-appear    (fn [this]
                                   ;; Executed when component appeared to the screen
                                   )
        :component-will-appear   (fn [this]
                                   ;; Executed when component will be shown to the screen
                                   )
        :component-did-disappear (fn [this]
                                   ;; Executed when component disappeared from the screen
                                   )}
       (fn [props & children] ; Must be the same signature as this `view` function
         ;; Regular component call, e.g.:
         [rn/view {:style {:padding-top 10}}
          [:f> my-f-component-call (assoc props :on-press identity)]
          children]))))
    "
  [component-id react-methods reagent-render]
  (reagent/create-class
   (assoc react-methods
          :display-name        (str component-id "-view")
          :component-did-mount #(navigation/bind-component % component-id)
          :reagent-render      reagent-render)))
