(ns status-im.commands.utils
  (:require [clojure.set :as set]
            [clojure.walk :as w]
            [status-im.components.react :as react-components]
            [re-frame.core :refer [dispatch trim-v debug subscribe]]
            [status-im.utils.handlers :refer [register-handler]]
            [cljs.js :as cljs]
            [clojure.string :as s]))

(def command-prefix "c ")

(defn json->clj [json]
  (when-not (= json "undefined")
    (js->clj (.parse js/JSON json) :keywordize-keys true)))

(def elements
  {:text        `react-components/text
   :view        `react-components/view
   :slider      `react-components/slider
   :scroll-view `react-components/scroll-view
   :web-view    `react-components/web-view
   :image       `react-components/image
   :touchable   `react-components/touchable-highlight})

(defn get-element [n]
  (elements (keyword (.toLowerCase n))))

(def events #{:onPress :onValueChange :onSlidingComplete})

(defn wrap-event [event]
  (let [data (gensym)]
    `(cljs.core/fn [~data] (dispatch [:suggestions-event! ~event ~data]))))

(defn check-events [m]
  (let [ks  (set (keys m))
        evs (set/intersection ks events)]
    (reduce #(update %1 %2 wrap-event) m evs)))

(def components-state
  {:name 'status-im.components.react
   :defs (reduce (fn [res el] (assoc res (symbol (name el)) {})) {} (vals elements))})
(defn empty-state []
  (cljs/empty-state
    (fn [state]
      (-> state
          (assoc-in [::cljs.analyzer/namespaces 're-frame.core]
                    {:name 're-frame.core
                     :defs {'dispatch  {}
                            'subscribe {}}})
          (assoc-in [::cljs.analyzer/namespaces 'status-im.components.react]
                    components-state)))))

(defn eval [code cb]
  (cljs/eval (empty-state) code {:eval cljs/js-eval} cb))

(defn parse-markup [markup]
  (let [subs (atom {})]
    [(w/prewalk
       (fn [el]
         (cond

           (and (vector? el) (= "subscribe" (first el)))
           (let [sub-name (gensym "sub")]
             (swap! subs assoc sub-name (second el))
             sub-name)

           (and (vector? el) (= "dispatch" (first el)))
           (-> el
               second
               (update 0 keyword))

           (and (vector? el) (string? (first el)))
           (-> el
               (update 0 get-element)
               (update 1 check-events))

           :esle el))
       markup)
     @subs]))

(defn generate-view-form [parsed-markup subs]
  `(defn ~(gensym) []
     (let [~@(mapcat (fn [[sym sub]]
                       [sym `(subscribe [:bot-subscription ~(mapv keyword sub)])])
                     subs)]
       ~(w/postwalk
          (fn [el]
            (if (and (symbol? el) (s/starts-with? (str el) "sub"))
              `(deref ~el)
              el))
          parsed-markup))))

(defn generate-hiccup [markup callback]
  (let [form (apply generate-view-form (parse-markup markup))]
    (eval form (fn [{:keys [value]}] (callback [value])))))

(defn reg-handler
  ([name handler] (reg-handler name nil handler))
  ([name middleware handler]
   (register-handler name [trim-v middleware] handler)))
