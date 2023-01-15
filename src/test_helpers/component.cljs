(ns test-helpers.component
  "Helpers for writing component tests using React Native Testing Library."
  (:require-macros test-helpers.component)
  (:require ["@testing-library/react-native" :as rtl]
            [camel-snake-kebab.core :as camel-snake-kebab]
            [reagent.core :as reagent]))

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

(defn get-by-text
  [text]
  (rtl/screen.getByText text))

(defn get-by-label-text
  [label]
  (rtl/screen.getByLabelText (name label)))

(defn expect [match] (js/expect match))
