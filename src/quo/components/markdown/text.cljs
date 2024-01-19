(ns quo.components.markdown.text
  (:require
    [quo.foundations.colors :as colors]
    [quo.foundations.typography :as typography]
    [quo.theme :as quo.theme]
    [react-native.pure :as rn.pure]))

(defn text-style
  [{:keys [size align weight style]} theme]
  (merge (case (or weight :regular)
           :regular   typography/font-regular
           :medium    typography/font-medium
           :semi-bold typography/font-semi-bold
           :bold      typography/font-bold
           :monospace typography/monospace
           :code      typography/code
           :inherit   nil)
         (case (or size :paragraph-1)
           :label       typography/label
           :paragraph-2 typography/paragraph-2
           :paragraph-1 typography/paragraph-1
           :heading-2   typography/heading-2
           :heading-1   typography/heading-1
           :inherit     nil)
         {:text-align (or align :auto)}
         (if (:color style)
           style
           (assoc style
                  :color
                  (if (= (or theme (quo.theme/get-theme)) :dark) colors/white colors/neutral-100)))))

(defn text-pure
  [props children]
  (let [theme (quo.theme/use-theme)
        style (text-style props theme)]
    (apply rn.pure/text
           (-> props
               (assoc :style style)
               (dissoc :size :align :weight :color :theme))
           children)))

(defn text
  [& children]
  (let [props (first children)
        props (when (map? props) props)]
    (rn.pure/func text-pure props (if props (rest children) children))))
