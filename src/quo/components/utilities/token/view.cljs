(ns quo.components.utilities.token.view
  (:require
    [clojure.string :as string]
    [quo.components.markdown.text :as quo]
    [quo.components.utilities.token.loader :as token-loader]
    [react-native.core :as rn]
    [schema.core :as schema]
    [utils.number]))

(def ?schema
  [:=>
   [:cat
    [:map {:closed true}
     [:size {:optional true :default 32} [:or keyword? pos-int?]]
     [:token {:optional true} [:or keyword? string?]]
     [:style {:optional true} map?]
     ;; Ignores `token` and uses this as parameter to `rn/image`'s source.
     [:image-source {:optional true} [:or :schema.common/image-source :string]]
     ;; If true, adds `data:image/png;base64,` as prefix to the string passed as `image-source`
     [:add-b64-prefix? {:optional true} boolean?]]]
   :any])

(defn- size->number
  "Remove `size-` prefix in size keywords and returns a number useful for styling."
  [size]
  (-> size name (subs 5) utils.number/parse-int))

(defn- token-style
  [style size]
  (let [size-number (if (keyword? size)
                      (size->number size)
                      size)]
    (assoc style
           :width  size-number
           :height size-number)))

(def ^:private b64-png-image-prefix "data:image/png;base64,")

(defn temp-empty-symbol
  [token size style]
  [rn/view
   {:style (token-style (merge {:justify-content :center
                                :align-items     :center
                                :border-radius   20
                                :border-width    1
                                :border-color    :grey}
                               style)
                        size)}
   [quo/text {:style {:color :grey}}
    (some-> token
            name
            first
            string/capitalize)]])

(defn view-internal
  "Render a token image.
   Props:
   - style:           extra styles to apply to the `rn/image` component.
   - size:            `:size-nn` or just `nn`, being `nn` any number. Defaults to 32.
   - token:           string or keyword, it can contain upper case letters or not.
                      E.g. all of these are valid and resolve to the same:
                      :token/snt | :snt | :SNT | \"SNT\" | \"snt\".
   - image-source:    Ignores `token` and uses this as parameter to `rn/image`'s source.
   - add-b64-prefix?: If true, adds `data:image/png;base64,` as prefix to the string
                      passed as `image-source`.
  "
  [{:keys [token size style image-source add-b64-prefix?]
    :or   {size 32}}]
  (let [b64-string (if (and image-source add-b64-prefix?)
                     (str b64-png-image-prefix image-source)
                     image-source)
        source     (or b64-string (token-loader/get-token-image token))]
    (if source
      [rn/image
       {:style  (token-style style size)
        :source source}]
      [temp-empty-symbol token size style])))

(def view (schema/instrument #'view-internal ?schema))
