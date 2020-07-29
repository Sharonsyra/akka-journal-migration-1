import java.sql.{Connection, DriverManager, ResultSet}

import com.google.protobuf.any.Any
import io.superflat.lagompb.encryption.{NoEncryption, ProtoEncryption}
import io.superflat.lagompb.protobuf.core.{EventWrapper, StateWrapper}
import io.superflat.lagompb.protobuf.encryption.EncryptedProto

import scala.util.Try

object Main extends App with ProtoEncryption {
  println("Postgres connector")

  classOf[org.postgresql.Driver]
  val con_st = "jdbc:postgresql://localhost:5432/postgres?user=postgres&password=changeme"
  val conn = DriverManager.getConnection(con_st)
  var people = new scala.collection.mutable.ListBuffer[(BigInt, String, BigInt, Boolean, String, Try[EncryptedProto])]()
  try {
    val stm = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)
    try {
      val rs = stm.executeQuery("SELECT * FROM journal")
      try {
        while (rs.next) {
          val person = (
            rs.getInt(1),
            rs.getString(2),
            rs.getInt(3),
            rs.getBoolean(4),
            rs.getString(5),
            encrypt(Any.pack(StateWrapper.parseFrom(rs.getBytes(6))))
          )

          val prep = conn.prepareStatement("INSERT INTO new_journal VALUES (?, ?, ?, ?, ?, ?) ")
          prep.setInt(1, person._1)
          prep.setString(2, s"${person._2}")
          prep.setInt(3, person._3)
          prep.setBoolean(4, person._4)
          prep.setString(5, s"${person._5}")
          prep.setObject(6, person._6.toString.getBytes())

          prep.executeUpdate

        }
      }
      finally {
        println("Migration starting...")

        stm.execute("ALTER TABLE journal RENAME TO old_journal")
        stm.execute("ALTER TABLE new_journal RENAME TO journal")

        val last_ordering_number =
          stm.executeQuery("SELECT ordering FROM journal ORDER BY ordering DESC LIMIT 1")

        while (last_ordering_number.next()) {
          val next_ordering_number = last_ordering_number.getInt(1) + 1

          stm.execute("ALTER TABLE journal " +
            "ALTER ordering SET NOT NULL, " +
            "ALTER ordering ADD GENERATED ALWAYS AS IDENTITY (START WITH "
            + next_ordering_number + ");")

          println("Migration is done")
        }

        last_ordering_number.close()
        rs.close()
      }

    }
    finally {
      stm.close()
      }
    }
  finally {
    conn.close()
  }

  override def encrypt(proto: Any) = {
    NoEncryption.encrypt(proto)
}

  override def decrypt(encryptedProto: EncryptedProto) = {
    NoEncryption.decrypt(encryptedProto)
  }
}
