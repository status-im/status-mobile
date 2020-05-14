(ns quo.react
  (:refer-clojure :exclude [ref])
  (:require [oops.core :refer [oget oset!]]
            ["react" :as react])
  (:require-macros [quo.react :refer [with-deps-check
                                      maybe-js-deps]]))

(def create-ref react/createRef)

(defn current-ref [ref]
  (oget ref "current"))

;; Inspired from UIX, Rum and Rumext
(defn set-ref-val!
  [ref val]
  (oset! ref "current" val)
  val)

(deftype StateHook [value set-value]
  cljs.core/IHash
  (-hash [o] (goog/getUid o))

  cljs.core/IDeref
  (-deref [o]
    value)

  cljs.core/IReset
  (-reset! [o new-value]
    (set-value new-value))

  cljs.core/ISwap
  (-swap! [o f]
    (set-value f))
  (-swap! [o f a]
    (set-value #(f % a)))
  (-swap! [o f a b]
    (set-value #(f % a b)))
  (-swap! [o f a b xs]
    (set-value #(apply f % a b xs))))

(defn state [value]
  (let [[value set-value] (react/useState value)
        sh                (react/useMemo #(StateHook. value set-value) #js [])]
    (react/useMemo (fn []
                     (set! (.-value sh) value)
                     (set! (.-set-value sh) set-value)
                     sh)
                   #js [value set-value])))

(defn use-ref [val]
  (let [ref (react/useRef val)]
    (reify
      cljs.core/IHash
      (-hash [_] (goog/getUid ref))

      cljs.core/IDeref
      (-deref [_]
        (current-ref ref))

      cljs.core/IReset
      (-reset! [_ new-value]
        (set-ref-val! ref new-value))

      cljs.core/ISwap
      (-swap! [_ f]
        (-reset! ref (f (current-ref ref))))
      (-swap! [_ f a]
        (-reset! ref (f (current-ref ref) a)))
      (-swap! [_ f a b]
        (-reset! ref (f (current-ref ref) a b)))
      (-swap! [_ f a b xs]
        (-reset! ref (apply f (current-ref ref) a b xs))))))

(defn ref [value]
  (let [vref (use-ref value)]
    (react/useMemo (fn [] vref) #js [])))

(defn effect!
  ([setup-fn]
   (react/useEffect
    #(let [ret (setup-fn)]
       (if (fn? ret) ret js/undefined))))
  ([setup-fn deps]
   (with-deps-check [prev-deps*]
     (react/useEffect
      (fn []
        (reset! prev-deps* deps)
        (let [ret (setup-fn)]
          (if (fn? ret) ret js/undefined)))
      (maybe-js-deps @prev-deps*))
     deps)))

(defn layout-effect!
  ([setup-fn]
   (react/useLayoutEffect
    #(let [ret (setup-fn)]
       (if (fn? ret) ret js/undefined))))
  ([setup-fn deps]
   (with-deps-check [prev-deps*]
     (react/useLayoutEffect
      (fn []
        (reset! prev-deps* deps)
        (let [ret (setup-fn)]
          (if (fn? ret) ret js/undefined)))
      (maybe-js-deps @prev-deps*))
     deps)))

(defn callback
  ([f] (react/useCallback f))
  ([f deps]
   (with-deps-check [prev-deps*]
     (react/useCallback f (maybe-js-deps @prev-deps*))
     deps)))

(defn memo
  ([f] (react/useMemo f))
  ([f deps]
   (with-deps-check [prev-deps*]
     (react/useMemo f (maybe-js-deps @prev-deps*))
     deps)))

(defn get-children [^js children]
  (->> children
       (react/Children.toArray)
       (into [])))
