#if (${PACKAGE_NAME} && ${PACKAGE_NAME} != "")package ${PACKAGE_NAME};#end
#parse("File Header.java")
import net.earthcomputer.multiconnect.impl.MixinHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(${NAME.replace("Accessor", "")}.class)
public interface ${NAME} {
    #set($NAME_NEEDED = true)
    #if (${ACCESSOR_NAME.length()} == 0)
        #if (${ACCESSED.replaceAll("[a-z]", "").length()} != ${ACCESSED.length()})
            #set($ACTUAL_ACCESSOR_NAME = ${ACCESSED})
            #set($NAME_NEEDED = false)
        #else
            #set($ACTUAL_ACCESSOR_NAME = ${StringUtils.removeAndHump(${ACCESSED.toLowerCase()}, "_")})
        #end
        #set($ACTUAL_ACCESSOR_NAME = ${StringUtils.capitalizeFirstLetter(${ACTUAL_ACCESSOR_NAME})})
        #set($PREFIX = "get")
        #set($ACTUAL_ACCESSOR_NAME = $PREFIX + $ACTUAL_ACCESSOR_NAME)
    #else
        #set($ACTUAL_ACCESSOR_NAME = $ACCESSOR_NAME)
    #end
    @Accessor#if (${NAME_NEEDED})("${ACCESSED}")#end
    #if (${STATIC} == "true")static #end${TYPE} ${ACTUAL_ACCESSOR_NAME}()#if (${STATIC} == "true") {
        return MixinHelper.fakeInstance();
    }#else;#end
}
