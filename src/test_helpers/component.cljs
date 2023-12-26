(ns test-helpers.component
  "Helpers for writing component tests using React Native Testing Library."
  (:require-macros [test-helpers.component])
  (:require ["@testing-library/react-native" :as rtl]
            [camel-snake-kebab.core :as camel-snake-kebab]
            [oops.core :as oops]
            [quo.theme :as quo.theme]
            [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [utils.i18n :as i18n]))

;;;; React Native Testing Library

(defn- with-node-or-screen
  "Wrap RN Testing Library `method-name` and call it either on a ReactTestInstance
  or directly on the screen object.

  `method-name` can be either the name of the native method, or a kebab case
  keyword.

  It is often necessary in REPL sessions to call methods on the returned
  instance of RNTL `render` method, otherwise you can get weird errors, like
  'Unable to find node on an unmounted component'. This happens because RNTL was
  mainly conceptualized to run inside a test runner that automatically cleans up
  everything after each test.

  Usage:
  (def get-by-text (wrap-screen-or-node :get-by-text))

  In another file, and with the REPL running with the Shadow-CLJS `:mobile`
  target:

    (comment
      ;; Consider using a shorter var name when playing in a REPL.
      (def component (h/render [quo/counter {} 50]))
      (h/get-by-text component \"50\")

      ;; Or without the node it works too, but it is only reliable inside
      ;; a test runner.
      (h/get-by-text \"50\"))
  "
  [method-name]
  (let [method-name (camel-snake-kebab/->camelCaseString method-name)]
    (fn [& args]
      (if (= js/Object (type (first args))) ; Check if it's a node instance.
        (let [method (aget (first args) method-name)]
          (apply method (clj->js (rest args))))
        (let [method (aget rtl/screen method-name)]
          (apply method (clj->js args)))))))

(defn render
  [component]
  (rtl/render (reagent/as-element component)))

(defn render-with-theme-provider
  [component theme]
  (rtl/render (reagent/as-element [quo.theme/provider {:theme theme} component])))

(def unmount
  "Unmount rendered component.
  Sometimes useful to be called in a REPL, but unnecessary when rendering
  components with Jest, since components are automatically unmounted after each
  test."
  (with-node-or-screen :unmount))

(def debug
  "Pretty-print to STDOUT the current component tree."
  (with-node-or-screen :debug))

(def within rtl/within)

(defn wait-for
  ([condition] (wait-for condition {}))
  ([condition options]
   (rtl/waitFor condition (clj->js options))))

(defn fire-event
  ([event-name node]
   (fire-event event-name node nil))
  ([event-name node data]
   (rtl/fireEvent
    node
    (camel-snake-kebab/->camelCaseString event-name)
    (clj->js data))))

;;; Queries: find-*
;;
;; find-* functions don't work in the REPL because the returned promise is
;; always rejected with ReferenceError: Can't find variable: MessageChannel
;;
;; For this reason, find-* functions only work within the Jest runtime, hence
;; using the wrapper function `with-node-or-screen` is unnecessary.

(def find-by-text (comp rtl/screen.findByText name))

;;; Queries that work with a REPL and with Jest

(def get-all-by-text (with-node-or-screen :get-all-by-text))
(def get-by-text (with-node-or-screen :get-by-text))
(def query-all-by-text (with-node-or-screen :query-all-by-text))
(def query-by-text (with-node-or-screen :query-by-text))

(def get-all-by-label-text (with-node-or-screen :get-all-by-label-text))
(def get-by-label-text (with-node-or-screen :get-by-label-text))
(def query-all-by-label-text (with-node-or-screen :query-all-by-label-text))
(def query-by-label-text (with-node-or-screen :query-by-label-text))

(def get-all-by-display-value (with-node-or-screen :get-all-by-display-value))
(def get-by-display-value (with-node-or-screen :get-by-display-value))
(def query-all-by-display-value (with-node-or-screen :query-all-by-display-value))
(def query-by-display-value (with-node-or-screen :query-by-display-value))

(def get-all-by-placeholder-text (with-node-or-screen :get-all-by-placeholder-text))
(def get-by-placeholder-text (with-node-or-screen :get-by-placeholder-text))
(def query-all-by-placeholder-text (with-node-or-screen :query-all-by-placeholder-text))
(def query-by-placeholder-text (with-node-or-screen :query-by-placeholder-text))

(def get-all-by-role (with-node-or-screen :get-all-by-role))
(def get-by-role (with-node-or-screen :get-by-role))
(def query-all-by-role (with-node-or-screen :query-all-by-role))
(def query-by-role (with-node-or-screen :query-by-role))

(def get-all-by-test-id (with-node-or-screen :get-all-by-test-id))
(def get-by-test-id (with-node-or-screen :get-by-test-id))
(def query-all-by-test-id (with-node-or-screen :query-all-by-test-id))
(def query-by-test-id (with-node-or-screen :query-by-test-id))

(defn get-all-by-translation-text
  ([translation]
   (get-all-by-translation-text rtl/screen translation nil))
  ([translation translation-opts]
   (get-all-by-translation-text rtl/screen translation translation-opts))
  ([^js node translation translation-opts & args]
   (apply (with-node-or-screen :get-all-by-text)
          node
          (i18n/label translation translation-opts)
          args)))

(defn get-by-translation-text
  ([translation]
   (get-by-translation-text rtl/screen translation nil))
  ([translation translation-opts]
   (get-by-translation-text rtl/screen translation translation-opts))
  ([^js node translation translation-opts & args]
   (apply (with-node-or-screen :get-by-text)
          node
          (i18n/label translation translation-opts)
          args)))

(defn query-by-translation-text
  ([translation]
   (query-by-translation-text rtl/screen translation nil))
  ([translation translation-opts]
   (query-by-translation-text rtl/screen translation translation-opts))
  ([^js node translation translation-opts & args]
   (apply (with-node-or-screen :query-by-text)
          node
          (i18n/label translation translation-opts)
          args)))

(defn query-all-by-translation-text
  ([translation]
   (query-all-by-translation-text rtl/screen translation nil))
  ([translation translation-opts]
   (query-all-by-translation-text rtl/screen translation translation-opts))
  ([^js node translation translation-opts & args]
   (apply (with-node-or-screen :query-all-by-text)
          node
          (i18n/label translation translation-opts)
          args)))

;;; Jest utilities

(def ^:private jest?
  (exists? js/jest))

(defn expect
  [match]
  (js/expect match))

(defn use-fake-timers
  []
  (when jest?
    (js/jest.useFakeTimers)))

(defn clear-all-timers
  []
  (when jest?
    (js/jest.clearAllTimers)))

(defn use-real-timers
  []
  (when jest?
    (js/jest.useRealTimers)))

(defn advance-timers-by-time
  [time-ms]
  (when jest?
    (js/jest.advanceTimersByTime time-ms)))

(def mock-fn
  (when jest?
    js/jest.fn))

(defn is-truthy
  [element]
  (.toBeTruthy (js/expect element)))

(defn is-falsy
  [element]
  (.toBeFalsy (js/expect element)))

(defn is-null
  [element]
  (.toBeNull (js/expect element)))

(defn is-disabled
  [element]
  (.toBeDisabled (js/expect element)))

(defn is-equal
  [element-1 element-2]
  (.toBe (js/expect element-1) element-2))

(defn was-called
  [mock]
  (.toHaveBeenCalled (js/expect mock)))

(defn was-called-with
  [mock expected-arg]
  (.toHaveBeenCalledWith (js/expect mock) expected-arg))

(defn was-called-times
  [^js mock number-of-times]
  (.toHaveBeenCalledTimes (js/expect mock) number-of-times))

(defn was-not-called
  [mock]
  (was-called-times mock 0))

(defn has-style
  [mock styles]
  (.toHaveStyle (js/expect mock) (clj->js styles)))

(defn has-prop
  ([element prop] (has-prop element prop js/undefined))
  ([element prop value]
   (.toHaveProp (js/expect element) (camel-snake-kebab/->camelCaseString prop) value)))

(defn get-rerender-fn
  "Returns a rerender function from React Native Testing Library.
   Takes a JS Object representing a component and returns a function that accepts hiccup
   representing the component to rerender with the new props to rerender it.
   e.g.
     (let [rerender-fn (h/get-rerender-fn
                        (h/render [my-component {:prop-1 :A
                                                 :prop-2 :B}]))]
       ;; Rerenders `my-component` with the new props
       (rerender-fn [my-component {:prop-1 :new-A
                                   :prop-2 :new-B}]))
  "
  [component]
  (fn [component-updated]
    (let [rerender-fn   (oops/oget component "rerender")
          react-element (reagent/as-element component-updated)]
      (rerender-fn react-element))))

(defn setup-subs
  "Registers `subscriptions`, a map of key (sub ID) to value (sub computation)."
  [subscriptions]
  (doseq [[sub-id v] subscriptions]
    (re-frame/reg-sub sub-id
     (fn [_] v))))

(defn setup-restorable-re-frame
  []
  (let [restorer (atom nil)]
    (js/beforeEach
     (fn []
       (reset! restorer (re-frame/make-restore-fn))))

    (js/afterEach
     (fn []
       (@restorer)))))

(defn setup-fake-timers
  []
  (js/beforeEach
   (fn []
     (use-fake-timers)))

  (js/afterEach
   (fn []
     (clear-all-timers)
     (use-real-timers))))
