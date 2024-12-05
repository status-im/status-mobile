(ns quo.components.utilities.token.view
  (:require
    [clojure.string :as string]
    [quo.components.utilities.token.loader :as token-loader]
    [react-native.core :as rn]
    [schema.core :as schema]
    [utils.number]))

(def ?schema
  [:=>
   [:cat
    [:map {:closed true}
     [:size {:optional true :default 32} [:or keyword? pos-int?]]
     [:token {:optional true} [:maybe [:or keyword? string?]]]
     [:style {:optional true} map?]
     ;; Ignores `token` and uses this as parameter to `rn/image`'s source.
     [:image-source {:optional true} [:maybe :schema.common/image-source]]]]
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
(def ^:const default-token-image (js/require "../resources/images/tokens/default-token.png"))

(defn- normalize-b64-string
  [b64-image]
  (if (string/starts-with? b64-image "data:image/")
    b64-image
    (str b64-png-image-prefix b64-image)))

(defn view-internal
  "Render a token image.
   Props:
   - style:        extra styles to apply to the `rn/image` component.
   - size:         `:size-nn` or just `nn`, being `nn` any number. Defaults to 32.
   - token:        string or keyword, it can contain upper case letters or not.
                   E.g. all of these are valid and resolve to the same:
                   :token/snt | :snt | :SNT | \"SNT\" | \"snt\".
   - image-source: Ignores `token` and uses this as parameter to `rn/image`'s source, it
                   can be a b64 string representing an image.
  "
  [{:keys [token size style image-source]
    :or   {size 32}}]
  (let [img-src     (if (string? image-source)
                      (normalize-b64-string image-source)
                      image-source)
        token-image (or img-src (token-loader/get-token-image token) default-token-image)]
    [rn/image
     {:style  (token-style style size)
      :source token-image}]))

(def view (schema/instrument #'view-internal ?schema))
