package org.example.compiler.plugin.fir

import org.jetbrains.kotlin.GeneratedDeclarationKey
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.EffectiveVisibility
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.containingClassForStaticMemberAttr
import org.jetbrains.kotlin.fir.declarations.FirResolvePhase
import org.jetbrains.kotlin.fir.declarations.builder.buildPrimaryConstructor
import org.jetbrains.kotlin.fir.declarations.builder.buildRegularClass
import org.jetbrains.kotlin.fir.declarations.builder.buildSimpleFunction
import org.jetbrains.kotlin.fir.declarations.impl.FirResolvedDeclarationStatusImpl
import org.jetbrains.kotlin.fir.declarations.origin
import org.jetbrains.kotlin.fir.extensions.FirDeclarationGenerationExtension
import org.jetbrains.kotlin.fir.extensions.MemberGenerationContext
import org.jetbrains.kotlin.fir.moduleData
import org.jetbrains.kotlin.fir.scopes.kotlinScopeProvider
import org.jetbrains.kotlin.fir.symbols.impl.*
import org.jetbrains.kotlin.fir.types.ConeTypeProjection
import org.jetbrains.kotlin.fir.types.builder.buildResolvedTypeRef
import org.jetbrains.kotlin.fir.types.impl.ConeClassLikeTypeImpl
import org.jetbrains.kotlin.name.*

/*
 * Generates top level class
 *
 * public final class foo.bar.MyClass {
 *     fun foo(): String = "Hello world"
 * }
 */
class SimpleClassGenerator(session: FirSession) : FirDeclarationGenerationExtension(session) {
    companion object {
        val MY_CLASS_ID = ClassId(FqName.fromSegments(listOf("foo", "bar")), Name.identifier("MyClass"))

        val FOO_ID = CallableId(MY_CLASS_ID, Name.identifier("foo"))
    }

    override fun generateClassLikeDeclaration(classId: ClassId): FirClassLikeSymbol<*>? {
        // Generate declaration only for the necessary ClassId (foo.bar.MyClass)
        if (classId != MY_CLASS_ID) return null
        val klass = buildRegularClass {
            moduleData = session.moduleData
            origin = Key.origin
            status = FirResolvedDeclarationStatusImpl(
                Visibilities.Public,
                Modality.FINAL,
                EffectiveVisibility.Public
            )
            classKind = ClassKind.CLASS
            scopeProvider = session.kotlinScopeProvider
            name = classId.shortClassName
            symbol = FirRegularClassSymbol(classId)
            superTypeRefs.add(session.builtinTypes.anyType)
        }
        return klass.symbol
    }

    // We need to convert the generated class into ConeTypeRef to assign the return
    // type for constructors and for dispatch receiver types
    private fun ClassId.toConeType(typeArguments: Array<ConeTypeProjection> = emptyArray()) =
        ConeClassLikeTypeImpl(ConeClassLikeLookupTagImpl(this), typeArguments, isNullable = false)

    override fun generateConstructors(context: MemberGenerationContext): List<FirConstructorSymbol> {
        val classId = context.owner.classId
        // We need to check if we are generating constructors only for the necessary ClassId (foo.bar.MyClass)
        require(classId == MY_CLASS_ID)
        val constructor = buildPrimaryConstructor {
            resolvePhase = FirResolvePhase.BODY_RESOLVE
            moduleData = session.moduleData
            origin = Key.origin
            returnTypeRef = buildResolvedTypeRef {
                type = classId.toConeType()
            }
            status = FirResolvedDeclarationStatusImpl(
                Visibilities.Public,
                Modality.FINAL,
                EffectiveVisibility.Public
            )
            symbol = FirConstructorSymbol(classId)
        }.also {
            it.containingClassForStaticMemberAttr = ConeClassLikeLookupTagImpl(classId)
        }
        return listOf(constructor.symbol)
    }

    // In this function we will generate only one function: fun foo(): String = "Hello world",
    // but other functions can be generated too
    override fun generateFunctions(
        callableId: CallableId,
        context: MemberGenerationContext?
    ): List<FirNamedFunctionSymbol> {
        val function = buildSimpleFunction {
            resolvePhase = FirResolvePhase.BODY_RESOLVE
            moduleData = session.moduleData
            origin = Key.origin
            status = FirResolvedDeclarationStatusImpl(
                Visibilities.Public,
                Modality.FINAL,
                EffectiveVisibility.Public
            )
            returnTypeRef = session.builtinTypes.stringType
            name = callableId.callableName
            symbol = FirNamedFunctionSymbol(callableId)
            // it's better to use default type on corresponding firClass to handle type parameters
            // but in this case we know that MyClass don't have any generics
            dispatchReceiverType = callableId.classId?.toConeType()
        }
        return listOf(function.symbol)
    }

    override fun getCallableNamesForClass(classSymbol: FirClassSymbol<*>) = if (classSymbol.classId == MY_CLASS_ID) {
        setOf(FOO_ID.callableName, SpecialNames.INIT)
    } else {
        emptySet()
    }

    override fun getTopLevelClassIds() = setOf(MY_CLASS_ID)

    override fun hasPackage(packageFqName: FqName) = packageFqName == MY_CLASS_ID.packageFqName

    // Don't forget to create this key
    object Key : GeneratedDeclarationKey()
}