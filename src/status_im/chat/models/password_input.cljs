(ns status-im.chat.models.password-input
  (:require [status-im.chat.constants :as const]
            [clojure.string :as str]
            [taoensso.timbre :as log]))

(defn- get-modified-text [text arg-pos]
  (let [hide-fn      #(apply str (repeat (count %) const/masking-char))
        updated-text (update text (inc arg-pos) hide-fn)]
    updated-text))

(defn- get-change [{:keys [command-name old-args new-args arg-pos selection]}]
  (let [old-args      (into [] old-args)
        new-args      (into [] new-args)
        modification  (- (count (get new-args arg-pos))
                         (count (get old-args arg-pos)))
        type          (if (> modification 0) :added :removed)
        position      (-> (:start selection)
                          (- (inc (count command-name)))
                          (- (count (str/join const/spacing-char (take arg-pos old-args))))
                          (- modification)
                          (- (if (= arg-pos 0) 0 1)))
        position      (if (= :added type) position (inc position))
        symbols-count (.abs js/Math modification)]
    {:type     type
     :position position
     :symbols  (when (= :added type)
                 (subs (get new-args arg-pos)
                       position
                       (+ position symbols-count)))}))

(defn- make-change [{:keys [command-name old-args new-args arg-pos selection] :as args}]
  (let [{:keys [type position symbols] :as c} (get-change args)
        make-change  #(if (= type :added)
                        (str (if % (subs % 0 position) "")
                             symbols
                             (if % (subs % position) ""))
                        (str (if % (subs % 0 position) "")
                             (if % (subs % (+ 1 position (count symbols))) "")))
        args         (if (= (count old-args) 0)
                       [const/spacing-char]
                       (into [] old-args))
        updated-args (update args arg-pos make-change)]
    (make-change (get args arg-pos))))

(def modifier
  {:execute-when      :hidden
   :make-change       make-change
   :get-modified-text get-modified-text})