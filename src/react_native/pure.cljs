(ns react-native.pure
  (:require ["react" :refer (Fragment useState createElement memo)]
            ["react-native" :refer
             (ActivityIndicator
              View
              Text
              TextInput
              SafeAreaView
              TouchableHighlight
              TouchableOpacity
              TouchableWithoutFeedback
              FlatList
              Pressable
              FlatList
              Image)]
            [reagent.impl.template :as reagent.template]))

(def use-state useState)

(defn get-create-element-fn
  [type]
  (fn [& children]
    (let [props (first children)]
      (if (map? props)
        (apply createElement type (reagent.template/convert-prop-value props) (rest children))
        (apply createElement type nil children)))))

(def view (get-create-element-fn View))
(def text (get-create-element-fn Text))
(def text-input (get-create-element-fn TextInput))
(def safe-area (get-create-element-fn SafeAreaView))
(def touchable (get-create-element-fn TouchableHighlight))
(def touchable-opacity (get-create-element-fn TouchableOpacity))
(def touchable-without-feedback (get-create-element-fn TouchableWithoutFeedback))
(def fragment (get-create-element-fn Fragment))
(def pressable (get-create-element-fn Pressable))
(def image-element (get-create-element-fn Image))
(def activity-indicator (get-create-element-fn ActivityIndicator))

(defn image
  [{:keys [source] :as props}]
  (image-element
   (if (string? source)
     ;;why are we doing this?
     (assoc props :source {:uri source})
     props)))

(def memo-fn-cache (atom {}))

(defn func
  [component-fn & props]
  (when-not (get @memo-fn-cache component-fn)
    (swap! memo-fn-cache assoc component-fn (memo #(apply component-fn (.-props %))
                                                  #(= (.-props %1) (.-props %2)))))
  (createElement (get @memo-fn-cache component-fn) #js {:props props}))

(defn flat-list
  [props]
  (createElement FlatList props))
