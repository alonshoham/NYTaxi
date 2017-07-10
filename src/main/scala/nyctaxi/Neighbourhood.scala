package nyctaxi

import org.openspaces.spatial.shapes.Shape

/**
  * Created by alons on 7/9/17.
  */
case class Neighbourhood (

                           var shape : Shape,
                           var properties : Map[String,String]

                         ) {

  def this() = this(null)

}
