# Avro Decoding Example Project #

I hope that this little GitHub project helps anyone who like I, did not find any useful answers by searching Google
for help with the constant ClassCastExceptions between `String` and `Utf8` that I encountered when trying to use Avro
with Java while deserializing specific domain objects from byte array messages.

To anyone who finds this repo while Google searching me for job hiring purposes, I apologize for the tone of this `README`.
It reflects my frustration with The Avro team's decision to architect with `Utf8`, which caused a lot of time to be spent
writing ugly conversion routines, ugly code and performing Google searches on how to resolve this issue. I'll likely
commit an update to this file with a gentler tone, but right now I'm still angry.

I do hope that this helps anyone out there who is facing a similar situation.

## The Problem ##

Inexplicably, the Avro team decided to use their own representation for Strings, the standard Java `String` class
apparently not being good enough for them. By default, Avro dashes off into left field and for generated classes, uses
something called the `Utf8` class. The name gives it a sheen of cross-platform respectability but they really should have
called it `SpecialAvroString` or something. While it does implement the "CharSequence" interface
(tragicomically, not until fairly recently), this is not nearly good enough to ease the pain this causes:

* The `CharSequence` contract does not guarantee equality for `equals(Object)` or `hashCode()` even for other `CharSequence`
implementations with the same content! The is perilous for widely-used Java collections such as `HashSet` and `HashMap`.
* Despite implementing `CharSequence`, String is still used the vast majority of time for quotidian tasks.

The above two issues lead to the tedious grind of performing conversions between `String` and `Utf8`. The code
is painful to both the eyes and the soul. Working with the default Avro-generated classes is a chore at best.

## Using Java's String Class in Avro ##

Luckily, the Avro team hacked in a some ways to use the standard Java String.
(Thanks to [this StackOverflow post](http://stackoverflow.com/questions/25118727/how-to-generate-fields-of-type-string-instead-of-charsequence-using-avro)!)

* On the command line, you can direct the Avro compiler to generate classes using the standard `String`:

    `java -jar ~/Downloads/avro-tools-1.7.7.jar compile -string schema ./avrosux.avsc ./avrosux`

* For the [Gradle plug-in](https://github.com/commercehub-oss/gradle-avro-plugin), you can add the `stringType` directive to the `avro` task, as I have done in this project:

        avro {
            stringType = "string"
        }

* For the Maven (ROFL) plugin, you can add the `stringType` configuration:

        <plugin>
            <groupId>org.apache.avro</groupId>
            <artifactId>avro-maven-plugin</artifactId>
            <version>1.7.7</version>
            <configuration>
                <stringType>String</stringType>
            </configuration>
        </plugin>

* The tragicomedy really starts with the Avro schema, where they instituted a little metaprogramming in the form of allowing you to annotate every instance of `"type": "string"` with a little *hint* to the Avro compiler:

		{
		    "name": "areYouKiddingMe",
		    "type": "string",
		    "avro.java.string": "String"
		}

	Let's never speak of this again, though. In a heterogeneous environment, you would be paraded through your cubicle farm like Cersei Lannister through the streets of King's Landing and pelted with garbage.

## But Wait There's More ##

In order to *decode* though, you must do something very specific. When working with data encoded using a Generic encoder, You have to specify a *different schema for reading than for writing*. Apparently, Avro does not look at your domain classes before deserializing them. Apparently, Avro does not look at the schema within your domain class before deserializing *unless explicitly told to*. See the code test for details.

