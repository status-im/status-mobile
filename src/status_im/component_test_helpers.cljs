(ns status-im.component-test-helpers
  "Helpers for writing component tests using React Native Testing Library."
  (:require-macros status-im.component-test-helpers)
  (:require ["@testing-library/react-native" :as rtl]
            [reagent.core :as reagent]
            [camel-snake-kebab.core :as camel-snake-kebab]))

(defn render
  [component]
  (rtl/render (reagent/as-element component)))

(defn fire-event
  [event-name element]
  (rtl/fireEvent element (camel-snake-kebab/->camelCaseString event-name)))

(defn debug
  [element]
  (rtl/screen.debug element))

(defn get-by-test-id
  [test-id]
  (rtl/screen.getByTestId (name test-id)))
