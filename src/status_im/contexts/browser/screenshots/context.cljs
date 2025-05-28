(ns status-im.contexts.browser.screenshots.context
  (:require
    ["react" :as react]
    [oops.core :as oops]
    [react-native.core :as rn]
    [status-im.navigation.screens :as screens]))

(defonce ^:private context (react/createContext nil))

(defn provider
  [data & children]
  (let [[screenshots set-screenshots] (rn/use-state {})
        add-screenshot-url            (fn [tab-id url]
                                        (set-screenshots (fn [state]
                                                           (assoc-in state [tab-id :url] url))))
        add-ref                       (fn [tab-id ref]
                                        (set-screenshots (fn [state]
                                                           (assoc-in state [tab-id :ref] ref))))]
    (into [:> (.-Provider context) {:value #js {:cljData data}}]
          children)))

(def default-data
  {:refs {}})

(defn- use-context-data
  []
  (if-let [data (rn/use-context context)]
    (oops/oget data :cljData)
    default-data))

(defn use-ref
  []
  (if-let [data (rn/use-context context)]
    (:theme (oops/oget data :cljData))
    :light))

(defn use-screen-id
  "A hook that returns the current screen id."
  []
  (when-let [data (rn/use-context context)]
    (:screen-id (oops/oget data :cljData))))

(defn use-screen-params
  "A hook that returns the current screen params"
  []
  (when-let [data (rn/use-context context)]
    (:screen-params (oops/oget data :cljData))))
