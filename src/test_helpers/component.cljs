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
  ([event-name element]
   (fire-event event-name element nil))
  ([event-name element data]
   (rtl/fireEvent
    element
    (camel-snake-kebab/->camelCaseString event-name)
    (clj->js data))))

(defn debug
  [element]
  (rtl/screen.debug element))

(defn get-by-test-id
  [test-id]
  (rtl/screen.getByTestId (name test-id)))

(defn get-by-text
  [text]
  (rtl/screen.getByText text))

(defn find-by-text
  [text]
  (rtl/screen.findByText text))

(defn get-by-label-text
  [label]
  (rtl/screen.getByLabelText (name label)))

(defn query-by-label-text
  [label]
  (rtl/screen.queryByLabelText (name label)))

(defn get-by-translation-text
  [keyword]
  (get-by-text (str "tx:" (name keyword))))

(defn get-all-by-label-text
  [label]
  (rtl/screen.getAllByLabelText (name label)))

(defn expect [match] (js/expect match))

(defn use-fake-timers [] (js/jest.useFakeTimers))

(defn clear-all-timers [] (js/jest.clearAllTimers))

(defn use-real-timers [] (js/jest.useRealTimers))

(defn advance-timers-by-time
  [time-ms]
  (js/jest.advanceTimersByTime time-ms))

(def mock-fn js/jest.fn)
